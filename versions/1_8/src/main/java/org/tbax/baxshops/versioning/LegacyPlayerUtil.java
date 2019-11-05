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
package org.tbax.baxshops.versioning;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class LegacyPlayerUtil
{
    private LegacyPlayerUtil()
    {
    }

    public static ItemStack getItemInHand(Player player)
    {
        return getItemInHand(player.getInventory());
    }

    public static void setItemInHand(Player player, ItemStack stack)
    {
        setItemInHand(player.getInventory(), stack);
    }

    public static void setItemInHand(PlayerInventory inventory, ItemStack stack)
    {
        inventory.setItemInHand(stack);
    }

    public static ItemStack getItemInHand(PlayerInventory inventory)
    {
        return inventory.getItemInHand();
    }

    public static ItemStack[] getInventoryContents(Player player)
    {
        return player.getInventory().getContents();
    }
}
