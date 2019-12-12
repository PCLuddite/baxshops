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
package org.tbax.baxshops.internal.items;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.internal.ShopPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Enchantable
{
    private String name;
    private Integer legacyId;
    private boolean hasLevels;

    public Enchantable(String name, boolean hasLevels)
    {
        this.name = name;
        this.hasLevels = hasLevels;
    }

    public Enchantable(int legacyId, String name, boolean hasLevels)
    {
        this.legacyId = legacyId;
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

    public int getLegacyId()
    {
        if (legacyId == null)
            throw new UnsupportedOperationException("This is not a legacy enchantment");
        return legacyId;
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

//    public static void reloadEnchantments(ShopPlugin plugin) throws IOException
//    {
//        YamlConfiguration yamlConfiguration = new YamlConfiguration();
//        ItemUtil.loadEnchants(plugin);
//        List<Map<String, Object>> list = new ArrayList<>();
//        for(Enchantment enchantment : Enchantment.values()) {
//            Map<String, Object> map = new HashMap<>();
//            Enchantable enchantable = ItemUtil.enchants.get(enchantment);
//            map.put("enchantment", enchantment.getName());
//
//            Map<String, String> key = new HashMap<>();
//            key.put("namespace", enchantment.getKey().getNamespace());
//            key.put("key", enchantment.getKey().getKey());
//            map.put("key", key);
//
//            if (enchantable == null) {
//                map.put("name", enchantment.getKey().getKey());
//                map.put("levels", enchantment);
//            }
//            else {
//                map.put("name", enchantable.getName());
//                map.put("levels", enchantable.hasLevels());
//                try {
//                    map.put("id", enchantable.getLegacyId());
//                }
//                catch (UnsupportedOperationException e) {
//                    // do nothing
//                }
//            }
//
//            list.add(map);
//        }
//        yamlConfiguration.set("enchants", list);
//        yamlConfiguration.save(new File(plugin.getDataFolder(), "enchants.yml"));
//    }
}
