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
package org.tbax.baxshops.nms.network.chat;

import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.nms.RuntimeObject;

public final class ChatMessageType extends RuntimeObject
{
    public static ChatMessageType a = new ChatMessageType("a");
    public static ChatMessageType b = new ChatMessageType("b");
    public static ChatMessageType c = new ChatMessageType("c");

    @Override
    public String __pkg_name()
    {
        return "net.minecraft.network.chat";
    }

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
