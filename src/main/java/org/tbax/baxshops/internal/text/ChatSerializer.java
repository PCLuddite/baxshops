/*
 * Copyright (C) 2013-2019 Timothy Baxendale
 * Portions derived from Shops Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus.
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
package org.tbax.baxshops.internal.text;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public final class ChatSerializer
{
    private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

    private ChatSerializer()
    {
    }

    private static Class<?> getChatSerializerClass() throws ClassNotFoundException
    {
        return Class.forName("net.minecraft.server." + VERSION + ".IChatBaseComponent$ChatSerializer");
    }

    private static Class<?> getChatComponentClass() throws ClassNotFoundException
    {
        return Class.forName("net.minecraft.server." + VERSION + ".IChatBaseComponent");
    }

    private static Class<?> getPacketClass() throws ClassNotFoundException
    {
        return Class.forName("net.minecraft.server." + VERSION + ".PacketPlayOutChat");
    }

    private static Object getNmsPlayer(Player player) throws ReflectiveOperationException
    {
        return player.getClass().getMethod("getHandle").invoke(player);
    }

    private static Object getNmsPlayerConnection(Player player) throws ReflectiveOperationException
    {
        Object nmsPlayer = getNmsPlayer(player);
        return nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
    }

    private static Object toNmsChatComponent(ChatComponent chatComponent) throws ReflectiveOperationException
    {
        Class<?> chatSerializer = getChatSerializerClass();
        return chatSerializer.getMethod("a", String.class).invoke(chatSerializer, chatComponent.toString());
    }

    public static void sendTo(Player player, ChatComponent chatComponent) throws ReflectiveOperationException
    {
        Constructor<?> packetConstructor = getPacketClass().getConstructor(getChatComponentClass());
        Object text = toNmsChatComponent(chatComponent);
        Object packetFinal = packetConstructor.newInstance(text);
        Field field = packetFinal.getClass().getDeclaredField("a");
        field.setAccessible(true);
        field.set(packetFinal, text);
        Object playerConnection = getNmsPlayerConnection(player);
        playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + VERSION + ".Packet")).invoke(playerConnection, packetFinal);
    }
}
