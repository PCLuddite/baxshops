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

import org.bukkit.potion.PotionType;

public final class PotionInfo
{
    private PotionType type;
    private String name;
    private String nbtName;
    private String upgradedNbtName;
    private String extendedNbtName;

    PotionInfo(PotionType type, String name, String nbtName, String upgradedNbtName, String extendedNbtName)
    {
        this.type = type;
        this.name = name;
        this.nbtName = nbtName;
        this.upgradedNbtName = upgradedNbtName;
        this.extendedNbtName = extendedNbtName;
    }

    public PotionType getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public String getNbtName()
    {
        return nbtName;
    }

    public String getUpgradedNbtName()
    {
        return upgradedNbtName;
    }

    public String getExtendedNbtName()
    {
        return extendedNbtName;
    }
}
