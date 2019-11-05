/*
 * Copyright (C) 2013-2019 Timothy Baxendale
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
package tbax.shops.serialization;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.Serializable;

public class BlockLocation implements Serializable
{
    private static final long serialVersionUID = 1L;
    int x;
    int y;
    int z;
    String worldName;

    public BlockLocation(final Location loc) {
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.worldName = loc.getWorld().getName();
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(this.worldName), this.x, this.y, this.z);
    }
}
