/* 
 * The MIT License
 *
 * Copyright © 2013-2015 Timothy Baxendale (pcluddite@hotmail.com) and 
 * Copyright © 2012 Nathan Dinsmore and Sam Lazarus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package tbax.baxshops.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Map;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.Main;

/**
 *
 * @author tbaxendale
 */
public final class BaxEntrySerializer
{
    public static JsonElement serialize(double version, BaxEntry entry) {
        if (version == 2.1) {
            return toJsonVer2_1(entry);
        }
        else if (version == 2.0) {
            return toJsonVer2_0(entry);
        }
        else if (version == 0.0) {
            return toJsonVer0(entry);
        }
        return null;
    }
    
    private static JsonElement toJsonVer0(BaxEntry entry) {
        JsonObject o = new JsonObject();
        o.addProperty("id", entry.getTypeId());
        o.addProperty("quantity", 
            entry.infinite ? -1 : entry.getAmount()
        );
        o.addProperty("damage", entry.getDurability());
        o.addProperty("retail", Main.roundTwoPlaces(entry.retailPrice));
        o.addProperty("refund", Main.roundTwoPlaces(entry.refundPrice));
        if (!entry.getEnchantments().isEmpty()) {
            JsonObject enchs = new JsonObject();
            for (Map.Entry<Enchantment, Integer> e : entry.getEnchantments().entrySet()) {
                enchs.addProperty(e.getKey().getId() + "", e.getValue());
            }
            o.add("enchantments", enchs);
        }
        return o;
    }
    
    private static JsonElement toJsonVer2_0(BaxEntry entry) {
        JsonObject o = new JsonObject();
        o.addProperty("id", entry.getType().name());
        o.addProperty("quantity", 
            entry.infinite ? -1 : entry.getAmount()
        );
        o.addProperty("damage", entry.getDurability());
        o.addProperty("retail", Main.roundTwoPlaces(entry.retailPrice));
        o.addProperty("refund", Main.roundTwoPlaces(entry.refundPrice));
        if (!entry.getEnchantments().isEmpty()) {
            JsonObject enchs = new JsonObject();
            for (Map.Entry<Enchantment, Integer> e : entry.getEnchantments().entrySet()) {
                enchs.addProperty(e.getKey().getName(), e.getValue());
            }
            o.add("enchantments", enchs);
        }
        return o;
    }
    
    private static JsonElement toJsonVer2_1(BaxEntry entry) {
        JsonObject o = new JsonObject();
        o.addProperty("id", entry.getType().name());
        o.addProperty("quantity", 
            entry.infinite ? -1 : entry.getAmount()
        );
        o.addProperty("damage", entry.getDurability());
        o.addProperty("retail", Main.roundTwoPlaces(entry.retailPrice));
        o.addProperty("refund", Main.roundTwoPlaces(entry.refundPrice));
        
        if (!entry.getEnchantments().isEmpty()) {
            JsonObject enchs = new JsonObject();
            for (Map.Entry<Enchantment, Integer> e : entry.getEnchantments().entrySet()) {
                enchs.addProperty(e.getKey().getName(), e.getValue());
            }
            o.add("enchantments", enchs);
        }
        
        if (entry.hasItemMeta()) {
            o.add("meta", metaToJsonVer2_1(entry.getItemMeta()));
        }
        return o;
    }
    
    private static JsonElement metaToJsonVer2_1(ItemMeta meta) {
        JsonObject o = new JsonObject();
        if (meta.hasDisplayName()) {
            o.addProperty("name", meta.getDisplayName());
        }
        if (meta.hasEnchants()) {
            JsonObject enchs = new JsonObject();
            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                enchs.addProperty(entry.getKey().getName(), entry.getValue());
            }
            o.add("enchantments", enchs);
        }
        if (meta.hasLore()) {
            JsonArray lore = new JsonArray();
            for(String l : meta.getLore()) {
                lore.add(new JsonPrimitive(l));
            }
            o.add("lore", lore);
        }
        if (!meta.getItemFlags().isEmpty()) {
            JsonArray flags = new JsonArray();
            for(ItemFlag flag : meta.getItemFlags()) {
                flags.add(new JsonPrimitive(flag.name()));
            }
            o.add("flags", flags);
        }
        return o;
    }
}
