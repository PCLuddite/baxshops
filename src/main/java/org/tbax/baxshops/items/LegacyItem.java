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
package org.tbax.baxshops.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class LegacyItem
{
    private int itemId;
    private boolean legacy;
    private String name;

    public LegacyItem(int itemId, String name, boolean legacy)
    {
        this.itemId = itemId;
        this.name = name;
        this.legacy = legacy;
    }

    public int getItemId()
    {
        return itemId;
    }

    public String getName()
    {
        return name;
    }

    public boolean isLegacy()
    {
        return legacy;
    }

    public ItemStack toItemStack(short damage)
    {
        Material material = Material.getMaterial(name, isLegacy());
        if (material == null) return null;
        return new ItemStack(material, 1, damage);
    }
}
