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

package com.chatjawa;

import com.chatjawa.data.Channel;
import com.chatjawa.data.ChatTab;
import com.chatjawa.data.Profile;
import com.chatjawa.misc.ImportDialog;
import com.chatjawa.utils.JawaUtils;
import com.chatjawa.utils.ProfileReader;
import com.chatjawa.utils.SwtorChatFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by Waverunner on 5/13/2015.
 */
public class ChatJawa extends Application {

    // TODO: Character cleanup tool for deleted characters
    // TODO: Color Presets

    private static ChatJawa instance;

    private MainInterfaceController mainController;
    private Stage stage;

    public static void main(String[] args) {
        //cleanup();
        launch(args);
    }

    public static ChatJawa getInstance() {
        return instance;
    }

    private static void cleanup() {
        new File("profiles").delete();
        new File("settings.xml").delete();
    }

    @Override
    public void start(Stage stage) throws Exception {
        instance = this;

        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("MainInterface.fxml").openStream());

        mainController = fxmlLoader.getController();
        if (mainController == null)
            JawaUtils.DisplayException(new Exception("Null Interface Controller"), "Interface Controller was null on startup.");

        initApp(stage, root);
    }

    private void initApp(Stage stage, Parent root) {
        this.stage = stage;

        Scene scene = new Scene(root);

        stage.setTitle("Chat Jawa");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            if (mainController.performSaveCheck()) {
                Platform.exit();
            }
        });

        load();

        stage.show();
    }

    private void load() {
        // TODO Check the version from properties and auto-update if necessary/enabled

        // Try and read the properties. If no settings file is found, then perform first-time setup.
        Properties properties = readProperties();
        if (properties == null) {
            firstLoad();
            return;
        }

        // Properties file found, add in the saved ChatJawa profiles

        List<Profile> profileList = new ArrayList<>();

        File profiles = new File("profiles");
        if (profiles.exists()) {
            ProfileReader reader = new ProfileReader();
            profileList = reader.read(profiles.listFiles());
        }

        if (profileList == null || profileList.size() < 0) {
            profileList = new ArrayList<>(Arrays.asList(getDefaultProfile()));
        }

        mainController.addProfiles(profileList);
    }

    private Properties readProperties() {

        // Read the main settings files
        Properties properties;

        File settings = new File("settings.xml");
        if (!settings.exists())
            return null;

        properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream(settings));
        } catch (IOException e) {
            e.printStackTrace();
            JawaUtils.DisplayException(e, "Error attempting to load the settings file: " + e.getLocalizedMessage());
            return null;
        }

        return properties;
    }

    private void createDependencies() {
        File profiles = new File("profiles");
        if (!profiles.exists())
            profiles.mkdir();
    }

    private void createProperties() {
        Properties properties = getDefaultProperties();
        try {
            properties.storeToXML(new FileOutputStream("settings.xml"), "Chat Jawa Settings");
        } catch (IOException e) {
            e.printStackTrace();
            JawaUtils.DisplayException(e, e.getLocalizedMessage());
        }
        System.setProperties(properties);
    }

    private void firstLoad() {

        createDependencies();
        // Create and set the default properties before continuing as ChatFactory uses them.
        createProperties();

        JawaUtils.DisplayInfo("This seems to be your first time using Chat Jawa", "Chat Jawa will automatically attempt to import all of your current"
                + " SWTOR character files that exist.If you notice any deleted characters attempting to be imported, you can cleanup"
                + " your settings directory through the Tools menu. "
                + " \n\nYou will be able to do all of these steps again through the Main Menu.");

        ImportDialog importDialog = new ImportDialog(SwtorChatFactory.getGameProfiles());
        importDialog.showAndWait().ifPresent(response -> {
            if (response == ImportDialog.IMPORT_BUTTON) {
                mainController.addProfiles(importDialog.getSelectedProfiles());
            } else {
                mainController.addProfiles(Arrays.asList(getDefaultProfile()));
            }
        });
    }

    private Profile getDefaultProfile() {
        Profile profile = new Profile("Default Profile");
        profile.setTimestamps(true);

        List<ChatTab> tabs = profile.getTabs();

        ChatTab general = new ChatTab("General");
        general.addChannel(Channel.GENERAL);
        general.addChannel(Channel.TRADE);

        ChatTab other = new ChatTab("Other");
        other.addChannel(Channel.CHAR_LOGIN);
        other.addChannel(Channel.GROUP_INFO);

        tabs.add(general);
        tabs.add(other);

        return profile;
    }

    private Properties getDefaultProperties() {
        Properties properties = new Properties();
        properties.setProperty("Version", "1");
        properties.setProperty("AutoUpdate", "true");
        properties.setProperty("GameSettings", System.getProperty("user.home") + "\\AppData\\Local\\SWTOR\\swtor\\settings");
        properties.setProperty("Profiles", new File("profiles").getAbsolutePath());
        return properties;
    }

    public MainInterfaceController getController() {
        return mainController;
    }

    public void setTitle(String name) {
        stage.setTitle("Chat Jawa - " + name);
    }

    public void close() {
        stage.close();
    }
}
