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

/**
 * Created by Waverunner on 5/16/2015
 */
public enum Server {
    EBON_HAWK(1098),
    PROPHECY_5(1086),
    NONE(0);

    private int id;

    Server(int id) {
        this.id = id;
    }

    public static Server get(int id) {
        for (Server server : Server.values()) {
            if (server.getId() == id)
                return server;
        }
        return Server.NONE;
    }

    public static Server get(String name) {
        for (Server server : Server.values()) {
            if (name.equals(server.toString()))
                return server;
        }
        return Server.NONE;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        // TODO Add all servers
        switch (this) {
            case EBON_HAWK:
                return "The Ebon Hawk";
            case PROPHECY_5:
                return "Prophecy of the Five";
            case NONE:
                return "No Server";
            default:
                return "Unknown Server Name";
        }
    }
}
