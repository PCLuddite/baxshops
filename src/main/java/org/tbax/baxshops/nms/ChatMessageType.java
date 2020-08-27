/*
 * Copyright (C) Timothy Baxendale
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.tbax.baxshops.nms;

import org.tbax.baxshops.ShopPlugin;

public final class ChatMessageType extends NmsObject
{
    public static ChatMessageType CHAT = new ChatMessageType("CHAT");
    public static ChatMessageType SYSTEM = new ChatMessageType("SYSTEM");
    public static ChatMessageType GAME_INFO = new ChatMessageType("GAME_INFO");

    private Object runtimeObject;

    private ChatMessageType(String name) {
        try {
            runtimeObject = __class().getField(name).get(null);
        } catch (ReflectiveOperationException e) {
            ShopPlugin.logWarning(e.getMessage() + " " + __class_name() + "." + name);
        }
    }

    @Override
    public Object __object() throws ReflectiveOperationException
    {
        return runtimeObject;
    }
}
