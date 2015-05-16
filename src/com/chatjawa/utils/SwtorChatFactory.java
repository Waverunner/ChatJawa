/*
 * ChatJawa is a Star Wars: The Old Republic tool for managing chat settings
 * across multiple characters.
 *
 * Copyright (C) 2015 ChatJawa
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.chatjawa.utils;

import com.chatjawa.data.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Waverunner on 5/13/2015
 */
public class SwtorChatFactory {
    private static final String CHAT_CHANNELS = "ChatChannels";
    private static final String PLAYER_GUISTATE_INI = "_PlayerGUIState.ini";
    private static final String SHOW_CHAT_TIME_STAMP = "Show_Chat_TimeStamp";

    // TODO Refactor to CharacterProfile

    public static List<Profile> getGameProfiles() {
        ArrayList<Profile> profiles = new ArrayList<>();

        File dir = new File(System.getProperty("GameSettings"));
        if (!dir.exists())
            return null;

        // Traverse through character setting files and create profiles for each one
        File[] files = dir.listFiles();
        if (files == null)
            return profiles;

        for (File file : files) {
            if (file.getName().endsWith(PLAYER_GUISTATE_INI)) {
                Profile profile = readProfileFile(file);
                if (profile != null)
                    profiles.add(profile);
            }
        }

        return profiles;
    }

    public static Profile readProfileFile(File file) {
        BufferedReader reader = null;
        try {
            // 0-7 = he1098_, length-19 = _PlayerGUIState.ini
            String name = file.getName().substring(7, file.getName().length() - 19);
            String server = file.getName().substring(2, file.getName().length() - (20 + name.length()));
            JawaUtils.Output("Server: " + server);
            reader = new BufferedReader(new FileReader(file));

            if (!reader.readLine().equalsIgnoreCase("[Settings]")) {
                JawaUtils.DisplayInfo("Import Warning", "Attempted to load a file that was not a valid settings file: \n" + file.getAbsolutePath());
                return null;
            }

            ArrayList<ChatTab> tabs = null;
            boolean timestamps = false;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty())
                    continue;

                if (line.startsWith(CHAT_CHANNELS)) {
                    tabs = readChatChannels(line);
                } else if (line.startsWith(SHOW_CHAT_TIME_STAMP)) {
                    timestamps = readChatTimestamp(line);
                }/* else if (line.startsWith("ChatColors")) {
                    // TODO Creating ColorProfile from Character Imports
                }*/
            }

            return createProfile(name, Server.get(Integer.valueOf(server)), tabs, null, timestamps);
        } catch (IOException ex) {
            JawaUtils.DisplayException(ex, ex.getLocalizedMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception ex) {
                JawaUtils.DisplayException(ex, ex.getLocalizedMessage());
            }
        }
        return null;
    }

    public static void save(Profile profile) {
        if (!(profile instanceof CharacterProfile))
            return;
        File dir = new File(System.getProperty("GameSettings"));
        if (!dir.exists()) {
            JawaUtils.DisplayWarning("Export Error", "Character Profiles were not saved because the settings location doesn't exist");
            return;
        }

        write(dir, (CharacterProfile) profile);
    }

    public static void save(List<Profile> profiles) {
        final File dir = new File(System.getProperty("GameSettings"));
        if (!dir.exists()) {
            JawaUtils.DisplayWarning("Export Error", "Character Profiles were not saved because the settings location doesn't exist");
            return;
        }

        profiles.forEach(profile -> {
            if (profile instanceof CharacterProfile)
                write(dir, (CharacterProfile) profile);
        });
    }

    private static void write(File dir, CharacterProfile profile) {
        String name = "he" + profile.getServer().getId() + "_" + profile.getName() + PLAYER_GUISTATE_INI;
        File file = new File(dir + "\\" + name);

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            if (!reader.readLine().equals("[Settings]")) {
                JawaUtils.DisplayWarning("Export Character", "No valid game settings file for " + profile.getName());
                return;
            }

            String line;

            // Grab the existing setting options minus the old chat settings
            List<String> strings = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (!(line.startsWith(SHOW_CHAT_TIME_STAMP) || line.startsWith(CHAT_CHANNELS))) {
                    strings.add(line);
                } else {
                    System.out.print(line);
                }
            }

            if (!writeChatSettings(file, strings, profile)) {
                JawaUtils.DisplayWarning("Export Character", "An issue occurred trying to export " + profile.getName() + ".\n"
                        + "\n\nA Backup file was created at " + file.getAbsolutePath() + ".bak");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JawaUtils.DisplayException(e, "Error trying to save character profile at line 166");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JawaUtils.Output("File: " + file.getAbsolutePath());
    }

    private static boolean writeChatSettings(File file, List<String> settings, CharacterProfile profile) {
        if (!createBackup(file))
            return false;

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file, false));
            writeLine(writer, "[Settings]");
            writeLine(writer, CHAT_CHANNELS + " = " + getChatChannelsString(profile));
            writeLine(writer, SHOW_CHAT_TIME_STAMP + " = " + String.valueOf(profile.isTimestampsEnabled()));
            for (String setting : settings) {
                writeLine(writer, setting);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    private static void writeLine(BufferedWriter writer, String line) throws Exception {
        writeLine(writer, line, true);
    }

    private static void writeLine(BufferedWriter writer, String line, boolean newLine) throws Exception {
        if (newLine) line += "\r\n";
        writer.write(line);
    }

    private static String getChatChannelsString(CharacterProfile profile) {
        String str = "";
        List<ChatTab> chatTabs = profile.getTabs();
        for (int i = 0; i < chatTabs.size(); i++) {
            ChatTab tab = profile.getTabs().get(i);
            str += String.valueOf(i) + "." + tab.getName() + "." + Channel.getId(tab.getChannels()) + ";";
        }
        return (str.isEmpty() ? null : str);
    }

    private static boolean createBackup(File file) {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            // Files.copy presented a ExceptionInInitializerError because of JavaFX bug perhaps

            File backup = new File(file.getAbsolutePath() + ".bak");
            if (!backup.exists())
                backup.createNewFile();

            inputStream = new FileInputStream(file);
            outputStream = new FileOutputStream(backup, false);

            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static Profile createProfile(String name, Server server, List<ChatTab> tabs, ColorProfile colors, boolean timestamps) {
        CharacterProfile profile = new CharacterProfile(name, server);
        profile.setTabs(tabs);
        profile.setColors(colors);
        profile.setTimestamps(timestamps);
        profile.setServer(server);
        return profile;
    }

    private static ArrayList<ChatTab> readChatChannels(String line) {
        line = line.replace(CHAT_CHANNELS + " = ", "");

        String[] strTabs = line.split(";");
        ArrayList<ChatTab> channels = new ArrayList<>();

        for (String s : strTabs) {
            // 0 = tabNum, 1 = tabName, 2 = tabChannels
            String[] settings = s.split("\\.");

            ChatTab tab = new ChatTab(settings[1]);
            tab.getChannels().addAll(Channel.getEnumeration(Long.valueOf(settings[2])));
            channels.add(tab);
        }

        return channels;
    }

    private static boolean readChatTimestamp(String line) {
        line = line.replace(SHOW_CHAT_TIME_STAMP + " = ", "");
        return Boolean.valueOf(line);
    }

/*    private static ColorProfile readChatColors(String line) {
        return null;
    }*/
}