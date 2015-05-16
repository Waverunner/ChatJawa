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

package com.chatjawa.misc;

import com.chatjawa.data.Profile;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Waverunner on 5/13/2015.
 */
public class ImportDialog extends Alert {

    public static ButtonType IMPORT_BUTTON = new ButtonType("Import", ButtonData.OK_DONE);
    private ObservableList<ProfileCellBox> profiles;

    public ImportDialog(List<Profile> characters) {
        super(AlertType.CONFIRMATION);

        this.setTitle("Chat Jawa");
        this.setHeaderText("Which characters should be imported?");

        ArrayList<ProfileCellBox> cells = new ArrayList<>();
        characters.forEach(profile -> cells.add(new ProfileCellBox(profile)));

        createDialogContent(cells);

        this.getButtonTypes().setAll(IMPORT_BUTTON, ButtonType.CANCEL);
    }

    private void createDialogContent(List<ProfileCellBox> cells) {
        // May be better for this to be an FXML
        HBox container = new HBox();

        // List view
        profiles = FXCollections.observableList(cells);
        ListView<ProfileCellBox> listView = new ListView<>(profiles);

        listView.setCellFactory(CheckBoxListCell.forListView(ProfileCellBox::selectedProperty, new StringConverter<ProfileCellBox>() {

            @Override
            public String toString(ProfileCellBox object) {
                return object.getData().getName();
            }

            @Override
            public ProfileCellBox fromString(String string) {
                return null;
            }
        }));

        container.getChildren().add(listView);
        container.getChildren().add(createButtonContainer());

        this.getDialogPane().setContent(container);
    }

    private VBox createButtonContainer() {
        // Selection Buttons
        VBox buttonContainer = new VBox();
        buttonContainer.alignmentProperty().setValue(Pos.TOP_LEFT);

        Button selectButton = new Button("Select All");
        selectButton.setOnAction(e -> {
            profiles.forEach(profile -> {
                profile.setSelected(true);
            });
        });

        Button deselectButton = new Button("Select None");
        deselectButton.setOnAction(e -> {
            profiles.forEach(profile -> {
                profile.setSelected(false);
            });
        });
        // Unclamp button sizes so they are uniform in the VBox
        selectButton.setMaxWidth(Double.MAX_VALUE);
        deselectButton.setMaxWidth(Double.MAX_VALUE);
        // Margins for eye-candy
        VBox.setMargin(selectButton, new Insets(5, 5, 5, 5));
        VBox.setMargin(deselectButton, new Insets(5, 5, 5, 5));

        buttonContainer.getChildren().add(selectButton);
        buttonContainer.getChildren().add(deselectButton);

        return buttonContainer;
    }

    public List<Profile> getSelectedProfiles() {
        ArrayList<Profile> selected = new ArrayList<>();

        profiles.stream().filter(profileCell -> profileCell.isSelected()).forEach(selection -> {
            // Set the profile as dirty since the character does not have a .profile file yet
            selection.getData().setDirty(true);
            selected.add(selection.getData());
        });

        return selected;
    }

    private static class ProfileCellBox {
        private final Profile data;

        private final ReadOnlyStringWrapper name;
        private final BooleanProperty selected;

        public ProfileCellBox(Profile data) {
            this.data = data;
            this.name = new ReadOnlyStringWrapper();
            this.selected = new SimpleBooleanProperty(false);

            this.name.set(data.getName());
        }

        public ReadOnlyStringProperty nameProperty() {
            return name.getReadOnlyProperty();
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        public Profile getData() {
            return data;
        }
    }
}
