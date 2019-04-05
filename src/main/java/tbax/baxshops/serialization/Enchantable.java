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
package tbax.baxshops.serialization;

import tbax.baxshops.Format;

public final class Enchantable
{
    private String name;
    private boolean hasLevels;

    public Enchantable(String name, boolean hasLevels)
    {
        this.name = name;
        this.hasLevels = hasLevels;
    }

    public String getName()
    {
        return name;
    }

    public boolean hasLevels()
    {
        return hasLevels;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public String toString(int levels)
    {
        if (hasLevels)
            return name + " " + Format.toNumeral(levels);
        return name;
    }
}
