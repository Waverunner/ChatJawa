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

import com.chatjawa.data.Channel;
import com.chatjawa.data.ChatTab;
import com.chatjawa.data.ColorProfile;
import com.chatjawa.data.Profile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Waverunner on 5/13/2015.
 */
public class SwtorChatFactory {
    public static List<Profile> getGameProfiles() {
        ArrayList<Profile> profiles = new ArrayList<>();

        File dir = new File(System.getProperty("GameGUIStates"));
        if (!dir.exists())
            return null;

        // Traverse through character setting files and create profiles for each one
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith("_PlayerGUIState.ini")) {
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
            reader = new BufferedReader(new FileReader(file));

            if (!reader.readLine().equalsIgnoreCase("[Settings]")) {
                Logger.getLogger(SwtorChatFactory.class.getName()).log(Level.WARNING, "{0} is not a valid settings file!", file.getAbsolutePath());
                JawaUtils.DisplayInfo("Import Warning", "Attempted to load a file that was not a valid settings file: \n" + file.getAbsolutePath());
                return null;
            }

            ColorProfile colors = null;
            ArrayList<ChatTab> tabs = null;
            boolean timestamps = false;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty())
                    continue;

                if (line.startsWith("ChatChannels")) {
                    tabs = readChatChannels(line);
                } else if (line.startsWith("Show_Chat_TimeStamp")) {
                    timestamps = readChatTimestamp(line);
                } else if (line.startsWith("ChatColors")) {
                    colors = readChatColors(line);
                }
            }

            return createProfile(name, tabs, colors, timestamps);
        } catch (IOException ex) {
            Logger.getLogger(SwtorChatFactory.class.getName()).log(Level.SEVERE, null, ex);
            JawaUtils.DisplayException(ex, ex.getLocalizedMessage());
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(SwtorChatFactory.class.getName()).log(Level.SEVERE, null, ex);
                JawaUtils.DisplayException(ex, ex.getLocalizedMessage());
            }
        }
        return null;
    }

    private static Profile createProfile(String name, List<ChatTab> tabs, ColorProfile colors, boolean timestamps) {
        Profile profile = new Profile(name);
        profile.setTabs(tabs);
        profile.setColors(colors);
        profile.setTimestamps(timestamps);
        profile.setCharacter(true);
        return profile;
    }

    private static ArrayList<ChatTab> readChatChannels(String line) {
        line = line.replace("ChatChannels = ", "");

        String[] strTabs = line.split(";");
        ArrayList<ChatTab> channels = new ArrayList<>();

        for (String s : strTabs) {
            // 0 = tabNum, 1 = tabName, 2 = tabChannels
            String[] settings = s.split("\\.");

            ChatTab tab = new ChatTab(settings[1]);
            // TODO: Read the channel data
            tab.getChannels().addAll(Channel.getEnumeration(Long.valueOf(settings[2])));
            channels.add(tab);
        }

        return channels;
    }

    private static boolean readChatTimestamp(String line) {
        line = line.replace("Show_Chat_TimeStamp = ", "");
        return Boolean.valueOf(line);
    }

    private static ColorProfile readChatColors(String line) {
        // TODO: Read chat colors from ini
        return null;
    }
}