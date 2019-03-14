/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.serialization.ItemNames;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class EnchantMap extends HashMap<Enchantment, Integer>
{
    private EnchantMap(Map<? extends Enchantment, ? extends Integer> m)
    {
        putAll(m);
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
            .map(e -> ItemNames.getEnchantName(e.getKey()).substring(0, 4).toUpperCase() + e.getValue())
            .collect(Collectors.toList());
        return String.join(",", names);
    }

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
            .map(e -> ItemNames.getEnchantName(e.getKey()))
            .collect(Collectors.toList());
        return String.join(", ", names);
    }

    @Override
    public String toString()
    {
        return abbreviatedListString(this);
    }
}
