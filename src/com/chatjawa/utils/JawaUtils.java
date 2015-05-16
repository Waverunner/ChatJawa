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

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Created by Waverunner on 5/13/2015.
 */
public class JawaUtils {
    public static void Output(String message) {
        System.out.println("[DEBUG] ChatJawa: " + message);
    }

    public static Optional<ButtonType> DisplayConfirmation(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Chat Jawa");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

        return alert.showAndWait();
    }

    public static void DisplayInfo(String header, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Chat Jawa");
        alert.setHeaderText(header);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void DisplayWarning(String header, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Chat Jawa");
        alert.setHeaderText(header);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void DisplayException(Throwable throwable, String message) {
        // TODO Add copy buttons to exception message

        // Create the dialog
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Chat Jawa");
        alert.setHeaderText("Chat Jawa has encountered an error.");
        alert.setContentText(message);

        // Create custom content
        Label label = new Label("Exception Stacktrace:");

        TextArea trace = new TextArea();
        trace.setEditable(false);
        trace.setWrapText(true);
        trace.setMaxHeight(Double.MAX_VALUE);
        trace.setMaxWidth(Double.MAX_VALUE);

        GridPane.setVgrow(trace, Priority.ALWAYS);
        GridPane.setHgrow(trace, Priority.ALWAYS);

        GridPane expandable = new GridPane();
        expandable.setMaxWidth(Double.MAX_VALUE);
        expandable.add(label, 0, 0);
        expandable.add(trace, 0, 1);

        // Set exception text
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        trace.setText(sw.toString());

        // Add to the dialog
        alert.getDialogPane().setExpandableContent(expandable);

        alert.showAndWait();
    }
}
