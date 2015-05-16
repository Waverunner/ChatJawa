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
import com.chatjawa.data.ColorProfile;
import com.chatjawa.data.Profile;
import com.chatjawa.misc.ChannelBoxListener;
import com.chatjawa.misc.ChatLabel;
import com.chatjawa.utils.JawaUtils;
import com.chatjawa.utils.ProfileWriter;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Waverunner on 5/13/2015
 */
public class MainInterfaceController implements Initializable {

    // <editor-fold defaultstate="collapsed" desc="Interface Components">
    @FXML
    private TabPane chatPane;
    @FXML
    private TreeView treeView;
    @FXML
    private Menu colorPresetsMenu;
    @FXML
    private Menu profilePresetsMenu;

    // Properties group
    @FXML
    private TextField profileTextField;
    @FXML
    private TextField parentTextField;
    @FXML
    private CheckBox characterProfileCheckBox;
    @FXML
    private CheckBox timeCheckBox;

    // Panes
    @FXML
    private TitledPane globalChannels;
    @FXML
    private TitledPane playerChannels;
    @FXML
    private TitledPane groupChannels;
    @FXML
    private TitledPane systemChannels;

    // Channel CheckBoxes

    // Global
    @FXML
    private CheckBox tradeCheckBox;
    @FXML
    private CheckBox pvpCheckBox;
    @FXML
    private CheckBox genChekBox;
    // Personal
    @FXML
    private CheckBox emoteCheckBox;
    @FXML
    private CheckBox yellCheckBox;
    @FXML
    private CheckBox officerCheckBox;
    @FXML
    private CheckBox guildCheckBox;
    @FXML
    private CheckBox sayCheckBox;
    @FXML
    private CheckBox whisperCheckBox;
    // Group
    @FXML
    private CheckBox opsCheckBox;
    @FXML
    private CheckBox opsLeaderCheckBox;
    @FXML
    private CheckBox groupCheckBox;
    @FXML
    private CheckBox opsAnnounceCheckBox;
    @FXML
    private CheckBox opsOfficerCheckBox;
    // System
    @FXML
    private CheckBox combatInfoCheckBox;
    @FXML
    private CheckBox conversationCheckBox;
    @FXML
    private CheckBox charLoginCheckBox;
    @FXML
    private CheckBox opsInfoCheckBox;
    @FXML
    private CheckBox sysFeedCheckBox;
    @FXML
    private CheckBox guildInfoCheckBox;
    @FXML
    private CheckBox groupInfoCheckBox;
    // </editor-fold>

    private Profile currentProfile;

    private boolean isChangingActiveTab = false;
    private boolean isChangingActiveProfile = false;

    private void populateFromProfile(Profile profile) {
        profileTextField.setText(profile.getName());
        parentTextField.setText(profile.getParent());
        timeCheckBox.setSelected(profile.isTimestampsEnabled());
        characterProfileCheckBox.setSelected(profile.isCharacter());

        populateTabs(profile.getTabs(), profile.getColors());

        if (profile.getParent().isEmpty()) {
            globalChannels.setVisible(true);
            playerChannels.setVisible(true);
            groupChannels.setVisible(true);
            systemChannels.setVisible(true);
        } else {
            globalChannels.setVisible(false);
            playerChannels.setVisible(false);
            groupChannels.setVisible(false);
            systemChannels.setVisible(false);
        }

        profilePresetsMenu.getItems().forEach(menuItem -> {
            if (menuItem.getUserData() == profile)
                menuItem.setDisable(true);
            else menuItem.setDisable(false);
        });
    }

    private void setProfileParent(Profile child, Profile parent) {
        if (child.getParent().equals(parent.getName()))
            return;

        if (parent == null && !child.getParent().isEmpty()) {
            child.setParent(null);
            profilePresetsMenu.getItems().add(createMenuItemForProfile(child));
            return;
        }

        child.copyFrom(parent, true);
        populateFromProfile(child);

        List<MenuItem> items = new ArrayList<>(profilePresetsMenu.getItems());

        items.stream().filter(menuItem -> menuItem.getUserData() == child).forEach(menuItem -> {
            profilePresetsMenu.getItems().remove(menuItem);
        });

        child.setDirty(true);
    }

    private void populateTabs(List<ChatTab> tabs, ColorProfile colors) {
        if (chatPane.getTabs() != null)
            chatPane.getTabs().clear();

        for (ChatTab tab : tabs) {
            createChatViewTab(tab, colors);
        }
    }

    private void createChatViewTab(ChatTab chatTab, ColorProfile colors) {
        ObservableList<Tab> tabs = chatPane.getTabs();
        Tab tab = new Tab(chatTab.getName());

        // TODO: Use a scroll view with a VBox

        // Create ChatLabel container and set the pref. size so labels properly wrap.
        VBox container = new VBox();
        container.setPrefSize(100, 200);
        tab.setContent(container);

        container.getChildren().addAll(createChatLabels(chatTab, colors));

        tab.setUserData(chatTab);

        tabs.add(tab);
    }

    private List<ChatLabel> createChatLabels(ChatTab tab, ColorProfile colors) {
        ArrayList<ChatLabel> labels = new ArrayList<>();

        tab.getChannels().forEach((Channel c) -> {
            labels.add(createChatLabel(c, colors));
        });

        return labels;
    }

    private ChatLabel createChatLabel(Channel chan, ColorProfile colors) {
        // TODO Random generation of text for chat labels
        return new ChatLabel("[" + chan.toString() + "]: TODO Random generation of text as well as adding a ScrollView to tab content");
    }

    private MenuItem createMenuItemForProfile(Profile profile) {
        MenuItem item = new MenuItem(profile.getName());
        item.setUserData(profile);

        item.setOnAction(e -> {
            setProfileParent(currentProfile, profile);
        });
        return item;
    }

    private void setChannelSelections(EnumSet<Channel> channels, boolean value) {
        channels.forEach((Channel c) -> {
            switch (c) {
                // Global
                case TRADE:
                    tradeCheckBox.setSelected(value);
                    break;
                case PVP:
                    pvpCheckBox.setSelected(value);
                    break;
                case GENERAL:
                    genChekBox.setSelected(value);
                    break;
                // Personal
                case EMOTE:
                    emoteCheckBox.setSelected(value);
                    break;
                case YELL:
                    yellCheckBox.setSelected(value);
                    break;
                case OFFICER:
                    officerCheckBox.setSelected(value);
                    break;
                case GUILD:
                    guildCheckBox.setSelected(value);
                    break;
                case SAY:
                    sayCheckBox.setSelected(value);
                    break;
                case WHISPER:
                    whisperCheckBox.setSelected(value);
                    break;
                // Group
                case OPS:
                    opsCheckBox.setSelected(value);
                    break;
                case OPS_LEADER:
                    opsLeaderCheckBox.setSelected(value);
                    break;
                case GROUP:
                    groupCheckBox.setSelected(value);
                    break;
                case OPS_ANNOUNCE:
                    opsAnnounceCheckBox.setSelected(value);
                    break;
                case OPS_OFFICER:
                    opsOfficerCheckBox.setSelected(value);
                    break;
                // System
                case COMBAT_INFO:
                    combatInfoCheckBox.setSelected(value);
                    break;
                case CONVERSATION:
                    conversationCheckBox.setSelected(value);
                    break;
                case CHAR_LOGIN:
                    charLoginCheckBox.setSelected(value);
                    break;
                case OPS_INFO:
                    opsInfoCheckBox.setSelected(value);
                    break;
                case SYS_FEEDBACK:
                    sysFeedCheckBox.setSelected(value);
                    break;
                case GUILD_INFO:
                    guildInfoCheckBox.setSelected(value);
                    break;
                case GROUP_INFO:
                    groupInfoCheckBox.setSelected(value);
                    break;
                default:
                    break;
            }
        });
    }

    //<editor-fold defaultstate="collapsed" desc="Handlers">
    private void handleSelectProfile(Profile profile) {
        if (profile == currentProfile)
            return;

        isChangingActiveProfile = true;
        populateFromProfile(profile);

        ChatJawa.getInstance().setTitle(profile.getName());

        currentProfile = profile;
        isChangingActiveProfile = false;
    }

    private void handleChangeProfileName(String newName) {
        currentProfile.setName(newName);
        ChatJawa.getInstance().setTitle(newName);

        currentProfile.setDirty(true);
    }

    private void handleViewChatTab(ChatTab activeTab) {
        JawaUtils.Output("Display data for tab: " + activeTab.getName());

        // Deselect any channel not enable for this tab.
        setChannelSelections(EnumSet.complementOf(activeTab.getChannels()), false);
        // Select rest of the channels.
        setChannelSelections(activeTab.getChannels(), true);
    }

    private boolean handleDisplaySaveDialog(List<Profile> unsavedProfiles) {
        String prompt = "The following profiles have changes that were not saved:\n\n";
        for (Profile profile : unsavedProfiles) {
            prompt += profile.getName() + "\n";
        }
        prompt += "\nWould you like to save the profiles before exiting?\n\n";

        AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        JawaUtils.DisplayConfirmation("Unsaved Changes", prompt).ifPresent(response -> {
            if (response == ButtonType.YES) {
                ProfileWriter parser = new ProfileWriter();
                parser.write(unsavedProfiles);
                atomicBoolean.set(true);
            } else if (response == ButtonType.CANCEL) {
                atomicBoolean.set(false);
            } else {
                atomicBoolean.set(true);
            }
        });
        return atomicBoolean.get();
    }
//</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ActionEvent Handlers">
    @FXML
    private void handleNoParentMenuItem(ActionEvent event) {
        setProfileParent(currentProfile, null);
    }

    @FXML
    private void handleColorSelect(ActionEvent event) {
        JawaUtils.Output("Color has been selected: " + event.getSource());
        if (!(event.getSource() instanceof ColorPicker)) {
            JawaUtils.Output("Not a ColorPicker!");
        }
//		ColorPicker dialog = (ColorPicker) event.getSource();
    }

    @FXML
    private void handleCreateColorPreset(ActionEvent event) {
        JawaUtils.Output("TODO: Create a color preset from current profile");
    }

    @FXML
    private void handleSaveProfileMenu(ActionEvent event) {
        if (currentProfile == null)
            return;

        ProfileWriter parser = new ProfileWriter();
        parser.write(currentProfile);
    }

    @FXML
    private void handleSaveAllProfilesMenu(ActionEvent event) {
        ProfileWriter parser = new ProfileWriter();
        parser.write(getProfiles());
    }

    @FXML
    private void handleExitMenu(ActionEvent event) {
        if (performSaveCheck())
            ChatJawa.getInstance().close();
    }

    // </editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Initializers">
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        treeView.setRoot(new TreeItem<>("Characters"));

        _addListeners();

    }

    private void _addListeners() {
        treeView.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
            TreeItem item = (TreeItem) newValue;

            // The root node only has a value that's a string.
            if (item.getValue() instanceof Profile)
                handleSelectProfile((Profile) item.getValue());
        });

        profileTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (profileTextField.getText().equals(currentProfile.getName()))
                return;

            handleChangeProfileName(profileTextField.getText());
        });

        chatPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue == oldValue)
                return;

            isChangingActiveTab = true;
            handleViewChatTab((ChatTab) newValue.getUserData());
            isChangingActiveTab = false;
        });

        _addCheckBoxListeners();
    }

    private void _addCheckBoxListeners() {
        // Profile Properties
        timeCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (currentProfile == null || isChangingActiveProfile)
                return;

            currentProfile.setDirty(true);
        });
        // General
        tradeCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.TRADE));
        pvpCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.PVP));
        genChekBox.selectedProperty().addListener(new ChannelBoxListener(Channel.GENERAL));
        // Personal
        emoteCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.EMOTE));
        yellCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.YELL));
        officerCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.OFFICER));
        guildCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.GUILD));
        sayCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.SAY));
        whisperCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.WHISPER));
        // Group
        opsCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.OPS));
        opsLeaderCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.OPS_LEADER));
        groupCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.GROUP));
        opsAnnounceCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.OPS_ANNOUNCE));
        opsOfficerCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.OPS_OFFICER));
        // System
        combatInfoCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.COMBAT_INFO));
        conversationCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.CONVERSATION));
        charLoginCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.CHAR_LOGIN));
        opsInfoCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.OPS_INFO));
        sysFeedCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.SYS_FEEDBACK));
        guildInfoCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.GUILD_INFO));
        groupInfoCheckBox.selectedProperty().addListener(new ChannelBoxListener(Channel.GROUP_INFO));
    }
//</editor-fold>

    public boolean performSaveCheck() {
        List<Profile> unsaved = new ArrayList<>();
        getProfiles().stream().filter(Profile::isDirty).forEach(profile -> unsaved.add(profile));

        if (unsaved.size() > 0)
            return handleDisplaySaveDialog(unsaved);

        return true;
    }

    public void addProfiles(List<Profile> profiles) {
        for (Profile p : profiles) {
            TreeItem<Profile> item = new TreeItem<>(p);

            treeView.getRoot().getChildren().add(item);
            profilePresetsMenu.getItems().add(0, createMenuItemForProfile(p));
        }
        treeView.getRoot().setExpanded(true);
    }

/*    public void setSelectedProfile(int index) {
        treeView.getSelectionModel().select(index);
    }

    public void setSelectedProfile(Profile profile) {
        for (Object item : treeView.getRoot().getChildren()) {
            if (item instanceof TreeItem) {
                if (((TreeItem) item).getValue() == profile) {
                    treeView.getSelectionModel().select(item);
                }
            }
        }
    }*/

    public List<Profile> getProfiles() {
        List<Profile> profiles = new ArrayList<>();
        treeView.getRoot().getChildren().forEach(item -> {
            if (item != null && item instanceof TreeItem) {
                Profile profile = ((TreeItem<Profile>) item).getValue();
                if (profile != null)
                    profiles.add(profile);
            }
        });
        return profiles;
    }

    public ChatTab getCurrentChatTab() {
        return (ChatTab) chatPane.getSelectionModel().selectedItemProperty().get().getUserData();
    }

    public Profile getCurrentProfile() {
        return currentProfile;
    }

    public boolean isChangingTabs() {
        return isChangingActiveTab;
    }
}
