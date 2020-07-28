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
package org.tbax.baxshops.serialization.states;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.ShopPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class StateLoader_00470 extends StateLoader_00460
{
    public static final double VERSION = 4.7;

    public StateLoader_00470(ShopPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public FileConfiguration readFile(@NotNull File stateLocation) throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(stateLocation))) {
            reader.readLine();
            return YamlConfiguration.loadConfiguration(reader);
        }
    }
}
