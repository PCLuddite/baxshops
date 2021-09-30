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
package org.tbax.baxshops.nms.network.protocol.game;

import org.tbax.baxshops.nms.network.chat.ChatMessageType;
import org.tbax.baxshops.nms.network.chat.IChatBaseComponent;
import org.tbax.baxshops.nms.network.protocol.Packet;

import java.lang.reflect.Constructor;
import java.util.UUID;

public final class PacketPlayOutChat extends Packet
{
    @Override
    public String __pkg_name()
    {
        return "net.minecraft.network.protocol.game";
    }

    private final Object runtimeObject;

    private static Constructor<?> ctor;
    public PacketPlayOutChat(IChatBaseComponent component, ChatMessageType chatMessageType, UUID uuid)
            throws ReflectiveOperationException
    {
        if (ctor == null) {
            ctor = __class().getConstructor(component.__class(), chatMessageType.__object().getClass(), UUID.class);
        }
        runtimeObject = ctor.newInstance(component.__object(), chatMessageType.__object(), uuid);
    }

    @Override
    public Object __object()
    {
        return runtimeObject;
    }
}
