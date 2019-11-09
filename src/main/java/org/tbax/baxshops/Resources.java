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
package org.tbax.baxshops;

import org.bukkit.ChatColor;

public final class Resources
{
    private Resources()
    {
    }
    /**
     * The distance from the sign in any direction which the player can go
     * before they leave the shop
     */
    public static final int SHOP_RANGE = 4 * 4;

    public static final String SHOP_EXISTS = "You can't create a new shop here! Another shop already exists on this block!";
    public static final String NOT_FOUND_SELECTED = "You do not have any shop selected!\nYou must select a shop to perform this action!";
    // Errors
    public static final String NOT_FOUND_SIGN = "You need a sign %s.";
    public static final String NOT_ONLINE = "The player needs to be online to use this command.";
    public static final String NO_ROOM_FOR_ITEM = "You do not have enough room for %d %s";
    public static final String NOT_FOUND_SHOPITEM = "That item has not been added to this shop.\nUse /shop add to add a new item";
    public static final String INVALID_DECIMAL = "The number entered for the %s is invalid.";
    public static final String NO_MONEY_SELLER = "The buyer does not have enough money";
    public static final String ERROR_INLINE = ChatColor.RED + "<ERROR>";
    public static final String INVALID_SHOP_ACTION = "'/shop %s' is not a valid action";
    // Info
    public static final String CURRENT_BALANCE = "Your current balance is %s.";
    public static final String SOME_ROOM = "Only " + ChatColor.RED + "%d %s" + ChatColor.RESET + " could fit in your inventory";
    public static final String NO_SUPPLIES = "There is not enough of it in the shop.";
    public static final String NOT_FOUND_NOTE = "You have no notifications for this action.";
}
