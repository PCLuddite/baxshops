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
package org.tbax.baxshops.internal.items;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class EnchantMap implements Map<Enchantment, Integer>
{
    private final Map<Enchantment, Integer> map;

    private EnchantMap(Map<Enchantment, Integer> m)
    {
        map = m;
    }

    public static Map<Enchantment, Integer> getEnchants(@NotNull ItemStack item)
    {
        if (!item.getEnchantments().isEmpty()) {
            return new EnchantMap(item.getEnchantments());
        }
        else if (item.hasItemMeta()) {
            if (item.getItemMeta().hasEnchants()) {
                return new EnchantMap(item.getItemMeta().getEnchants());
            }
            else if (item.getItemMeta() instanceof EnchantmentStorageMeta) {
                return new EnchantMap(((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants());
            }
        }
        return null;
    }

    public static boolean isEnchanted(@NotNull ItemStack stack)
    {
        Map<Enchantment, Integer> enchantMap = getEnchants(stack);
        return !(enchantMap == null || enchantMap.isEmpty());
    }

    public static @NotNull String abbreviatedListString(@NotNull ItemStack stack)
    {
        Map<Enchantment, Integer> enchants = getEnchants(stack);
        if (enchants == null)
            return "";
        return abbreviatedListString(enchants);
    }

    public static @NotNull String abbreviatedListString(@NotNull Map<Enchantment, Integer> enchants)
    {
        List<String> names = enchants.entrySet().stream()
            .map(e -> ItemUtil.getEnchantName(e.getKey()).substring(0, 4).toUpperCase() + e.getValue())
            .collect(Collectors.toList());
        return String.join(",", names);
    }

    @SuppressWarnings("unused")
    public static @NotNull String fullListString(@NotNull ItemStack stack)
    {
        Map<Enchantment, Integer> enchants = getEnchants(stack);
        if (enchants == null)
            return "";
        return fullListString(enchants);
    }

    public static String fullListString(@NotNull Map<Enchantment, Integer> enchants)
    {
        List<String> names = enchants.entrySet().stream()
            .map(e -> ItemUtil.getEnchantable(e.getKey()).toString(e.getValue()))
            .collect(Collectors.toList());
        return String.join(", ", names);
    }

    @Override
    public String toString()
    {
        return abbreviatedListString(this);
    }

    @Override
    public int size()
    {
        return map.size();
    }

    @Override
    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return map.containsValue(value);
    }

    @Override
    public Integer get(Object key)
    {
        return map.get(key);
    }

    @Override
    public @Nullable Integer put(Enchantment key, Integer value)
    {
        return map.put(key, value);
    }

    @Override
    public Integer remove(Object key)
    {
        return map.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends Enchantment, ? extends Integer> m)
    {
        map.putAll(m);
    }

    @Override
    public void clear()
    {
        map.clear();
    }

    @Override
    public @NotNull Set<Enchantment> keySet()
    {
        return map.keySet();
    }

    @Override
    public @NotNull Collection<Integer> values()
    {
        return map.values();
    }

    @Override
    public @NotNull Set<Entry<Enchantment, Integer>> entrySet()
    {
        return map.entrySet();
    }
}
