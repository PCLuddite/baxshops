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
import tbax.baxshops.serialization.ItemNames;

import java.util.HashMap;
import java.util.Map;

public final class EnchantMap extends HashMap<Enchantment, Integer>
{
    private EnchantMap(Map<? extends Enchantment, ? extends Integer> m)
    {
        putAll(m);
    }

    public static Map<Enchantment, Integer> getEnchants(ItemStack item)
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

    @Override
    public String toString()
    {
        if (isEmpty())
            return "";
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for(Entry<Enchantment, Integer> ench : entrySet()) {
            if (first) {
                first = false;
            }
            else {
                sb.append(", "); // separated by commas
            }
            sb.append(ItemNames.getEnchantName(ench.getKey()).toUpperCase(), 0, 4); // List each enchantment
            sb.append(ench.getValue()); // and its value
        }
        return sb.toString();
    }
}
