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

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Waverunner on 5/15/2015.
 */
public class ProfileWriter {

    private String profilesDir;

    private XMLOutputFactory outputFactory;
    private XMLEventFactory eventFactory;

    private XMLEvent lineEvent;
    private XMLEvent indentEvent;

    public ProfileWriter() {
        this.profilesDir = System.getProperty("Profiles") + "\\";
        this.outputFactory = XMLOutputFactory.newFactory();
        this.eventFactory = XMLEventFactory.newFactory();
        this.lineEvent = eventFactory.createDTD("\n");
        this.indentEvent = eventFactory.createDTD("\t");
    }

    public void write(Profile profile) {
        try {
            File file = new File(profilesDir + profile.getName() + ".profile");
            file.createNewFile();

            XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new FileOutputStream(file));
            eventWriter.add(eventFactory.createStartDocument("UTF-8"));
            eventWriter.add(lineEvent);

            createProfileElements_v1(eventWriter, profile);

            eventWriter.add(eventFactory.createEndDocument());
            eventWriter.close();
        } catch (Exception e) {
            JawaUtils.DisplayException(e, e.getLocalizedMessage());
        }
    }

    public void write(List<Profile> profileList) {
        profileList.forEach(profile -> write(profile));
    }

    private void createProfileElements_v1(XMLEventWriter eventWriter, Profile profile) throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("Name", profile.getName());
        attributes.put("Parent", profile.getParent());
        attributes.put("Timestamps", String.valueOf(profile.isTimestampsEnabled()));
        attributes.put("CharacterProfile", String.valueOf(profile.isCharacter()));

        createElement(eventWriter, "Profile", attributes);
        for (ChatTab tab : profile.getTabs()) {
            Map<String, String> tabAttributes = new HashMap<>();
            tabAttributes.put("Name", tab.getName());

            createIndent(eventWriter, 1);
            createElement(eventWriter, "ChatTab", tabAttributes);

            createChannelElements_v1(eventWriter, tab.getChannels());

            createIndent(eventWriter, 1);
            endElement(eventWriter, "ChatTab");
        }
        endElement(eventWriter, "Profile");
    }

    private void createChannelElements_v1(XMLEventWriter eventWriter, EnumSet<Channel> channels) throws Exception {
        for (Channel channel : channels) {
            createNode(eventWriter, "Channel", String.valueOf(channel.getId()), 2);
        }
    }

    private void createNode(XMLEventWriter eventWriter, String name, String value, int indents) throws Exception {

        // Start node
        StartElement sElement = eventFactory.createStartElement("", "", name);
        createIndent(eventWriter, indents);
        eventWriter.add(sElement);

        // Value
        Characters characters = eventFactory.createCharacters(value);
        eventWriter.add(characters);

        // End node
        EndElement eElement = eventFactory.createEndElement("", "", name);
        eventWriter.add(eElement);

        eventWriter.add(lineEvent);
    }

    private void createElement(XMLEventWriter eventWriter, String name) throws Exception {
        eventWriter.add(eventFactory.createStartElement("", "", name));
        eventWriter.add(lineEvent);
    }

    private void createElement(XMLEventWriter eventWriter, String name, Map<String, String> attributes) throws Exception {
        eventWriter.add(eventFactory.createStartElement("", "", name));
        attributes.forEach((k, v) -> {
            try {
                eventWriter.add(eventFactory.createAttribute(k, v));
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        });
        eventWriter.add(lineEvent);
    }

    private void endElement(XMLEventWriter eventWriter, String name) throws Exception {
        eventWriter.add(eventFactory.createEndElement("", "", name));
        eventWriter.add(lineEvent);
    }

    private void createLineTab(XMLEventWriter eventWriter) throws Exception {
        eventWriter.add(lineEvent);
        eventWriter.add(indentEvent);
    }

    private void createIndent(XMLEventWriter eventWriter, int level) throws Exception {
        for (int i = 0; i < level; i++) {
            eventWriter.add(indentEvent);
        }
    }
}
