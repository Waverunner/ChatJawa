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

package com.chatjawa.data;

import java.util.EnumSet;

/**
 * Created by Waverunner on 5/13/2015.
 */
public enum Channel {
    // Global
    TRADE(128),
    PVP(256),
    GENERAL(64),
    // Personal
    EMOTE(4),
    YELL(2),
    OFFICER(2048),
    GUILD(1024),
    SAY(1),
    WHISPER(8),
    // Group
    OPS(4096),
    OPS_LEADER(53680912),
    GROUP(512),
    OPS_ANNOUNCE(8589934592L),
    OPS_OFFICER(8192),
    // System
    COMBAT_INFO(137438953427L),
    CONVERSATION(524288),
    CHAR_LOGIN(1048576),
    OPS_INFO(17179869184L),
    SYS_FEEDBACK(262144),
    GUILD_INFO(68719476736L),
    GROUP_INFO(34359738368L);

    public static final String NO_IDENTIFIER = "NONE";
    private final long id;

    Channel(long id) {
        this.id = id;
    }

    public static EnumSet<Channel> getEnumeration(long id) {
        EnumSet<Channel> flags = EnumSet.noneOf(Channel.class);

        EnumSet.allOf(Channel.class).forEach(channel -> {
            long value = channel.getId();
            if ((value & id) == value) {
                flags.add(channel);
            }
        });
        return flags;
    }

    public static long getTabId(EnumSet<Channel> channels) {
        long value = 0;

        for (Channel channel : channels) {
            value |= channel.getId();
        }

        return value;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        // TODO: Channel enum names as displayed in the chat boxes
        switch (this) {
            // Global
            case TRADE:
                return "Trade";
            case PVP:
                return "PvP";
            case GENERAL:
                return "General";
            // Personal
            // Group
            // System
            default:
                return this.name();//NO_IDENTIFIER;
        }
    }
}
