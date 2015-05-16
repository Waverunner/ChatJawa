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
import com.chatjawa.data.Profile;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Waverunner on 5/15/2015.
 */
public class ProfileReader {

    private XMLInputFactory inputFactory;

    public ProfileReader() {
        this.inputFactory = XMLInputFactory.newFactory();
    }

    private void setProfileAttributes_v1(StartElement element, Profile profile) {
        Iterator<Attribute> iterator = element.getAttributes();
        while (iterator.hasNext()) {
            Attribute attribute = iterator.next();
            switch (attribute.getName().toString()) {
                case ProfileWriter.NAME:
                    profile.setName(attribute.getValue());
                    break;
                case ProfileWriter.P_CHARACTER:
                    profile.setCharacter(Boolean.valueOf(attribute.getValue()));
                    break;
                case ProfileWriter.P_TIMESTAMPS:
                    profile.setTimestamps(Boolean.valueOf(attribute.getValue()));
                    break;
                case ProfileWriter.P_PARENT:
                    profile.setParent(attribute.getValue());
                    break;
                default:
                    break;
            }
        }
    }

    private ChatTab setChatTabData_v1(StartElement element, Profile profile) throws Exception {
        ChatTab chatTab = null;
        Iterator<Attribute> iterator = element.getAttributes();
        while (iterator.hasNext()) {
            Attribute attribute = iterator.next();
            switch (attribute.getName().toString()) {
                case ProfileWriter.NAME:
                    chatTab = new ChatTab(attribute.getValue());
                    break;
                default:
                    break;
            }
        }
        return chatTab;
    }

    public Profile read(File file) {
        Profile profile = null;
        try {
            profile = new Profile(file.getName());
            XMLEventReader eventReader = inputFactory.createXMLEventReader(new FileInputStream(file));

            ChatTab chatTab = null;
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement element = event.asStartElement();

                    switch (element.getName().getLocalPart()) {
                        case ProfileWriter.PROFILE:
                            setProfileAttributes_v1(element, profile);
                            break;
                        case ProfileWriter.CHAT_TAB:
                            chatTab = setChatTabData_v1(element, profile);
                            if (chatTab != null)
                                profile.getTabs().add(chatTab);
                            break;
                        case ProfileWriter.CHANNEL:
                            if (chatTab != null) {
                                chatTab.addChannel(Channel.getValue(Long.valueOf(eventReader.nextEvent().asCharacters().getData())));
                            } else
                                JawaUtils.DisplayWarning("Read Issue", "Tried to set channel for a null channel");
                            break;
                        default:
                            JawaUtils.Output("Unsure how to handle: " + element.getName().getLocalPart());
                            break;
                    }
                }
            }
        } catch (Exception e) {
            JawaUtils.DisplayException(e, "Exception while reading Chat Profile - " + e.getLocalizedMessage());
        }

        return profile;
    }

    public List<Profile> read(File[] files) {
        List<Profile> profiles = new ArrayList<>();
        for (File file : files) {
            if (!file.getName().endsWith(".profile"))
                continue;

            profiles.add(read(file));
        }
        return profiles;
    }
}
