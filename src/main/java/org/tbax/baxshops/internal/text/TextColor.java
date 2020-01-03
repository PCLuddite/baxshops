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
package org.tbax.baxshops.internal.text;

import org.bukkit.ChatColor;

public enum TextColor
{
    BLACK("black", ChatColor.BLACK),
    DARK_BLUE("dark_blue", ChatColor.DARK_BLUE),
    DARK_GREEN("dark_green", ChatColor.DARK_GREEN),
    DARK_CYAN("dark_aqua", ChatColor.DARK_AQUA),
    DARK_RED("dark_red", ChatColor.DARK_RED),
    PURPLE("dark_purple", ChatColor.DARK_PURPLE),
    GOLD("gold", ChatColor.GOLD),
    GRAY("gray", ChatColor.GRAY),
    DARK_GRAY("dark_gray", ChatColor.DARK_GRAY),
    BLUE("blue", ChatColor.BLUE),
    BRIGHT_GREEN("green", ChatColor.BLUE),
    CYAN("aqua", ChatColor.AQUA),
    RED("red", ChatColor.RED),
    PINK("light_purple", ChatColor.LIGHT_PURPLE),
    YELLOW("yellow", ChatColor.YELLOW),
    WHITE("white", ChatColor.WHITE);

    private String value;
    private ChatColor chatColor;

    TextColor(String value, ChatColor chatColor)
    {
        this.value = value;
        this.chatColor = chatColor;
    }

    public ChatColor getChatColor()
    {
        return chatColor;
    }

    @Override
    public String toString()
    {
        return value;
    }
}
