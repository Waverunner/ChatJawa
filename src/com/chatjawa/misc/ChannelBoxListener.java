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

import com.chatjawa.ChatJawa;
import com.chatjawa.MainInterfaceController;
import com.chatjawa.data.Channel;
import com.chatjawa.data.ChatTab;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Created by Waverunner on 5/13/2015.
 */
public class ChannelBoxListener implements ChangeListener {

    private final Channel channel;

    public ChannelBoxListener(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        MainInterfaceController controller = ChatJawa.getInstance().getController();

        if (controller == null || controller.getCurrentProfile() == null || controller.isChangingTabs() || oldValue == newValue)
            return;

        ChatTab currentTab = controller.getCurrentChatTab();
        if (currentTab == null)
            return;

        if (!currentTab.getChannels().contains(channel) && (boolean) newValue == true)
            currentTab.addChannel(channel);
        else
            currentTab.getChannels().remove(channel);
    }

}
