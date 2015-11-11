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
import java.util.ArrayList;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tbax.baxshops.BaxEntry;

/**
 *
 * @author tbaxendale
 */
public final class BaxEntryDeserializer
{       
    public static BaxEntry deserialize(double version, JsonObject item) {
        if (version == 2.1) {
            return fromJsonVer2_1(item);
        }
        else if (version == 2.0) {
            return fromJsonVer2_0(item);
        }
        else if (version == 0.0) {
            return fromJsonVer0(item);
        }
        return null;
    }
    
    private static BaxEntry fromJsonVer2_1(JsonObject o) {
        BaxEntry entry = new BaxEntry();
        ItemStack stack = new ItemStack(Material.getMaterial(o.get("id").getAsString()), 1, o.get("damage").getAsShort());
        if (o.has("enchantments")) {
            for(Map.Entry<String, JsonElement> e : o.getAsJsonObject("enchantments").entrySet()) {;
                stack.addEnchantment(Enchantment.getByName(e.getKey()), e.getValue().getAsInt());
            }
        }
        if (o.has("meta")) {
            metaFromJsonVer2_1(stack.getItemMeta(), o.getAsJsonObject("meta"));
        }
        entry.setItem(stack);
        entry.retailPrice = o.get("retail").getAsDouble();
        entry.refundPrice = o.get("refund").getAsDouble();
        int num = o.get("quantity").getAsInt();
        if (num < 0) {
            entry.infinite = true;
        }
        else {
            entry.setAmount(num);
        }
        return entry;
    }
    
    private static ItemMeta metaFromJsonVer2_1(ItemMeta meta, JsonObject o) {
        if (o.has("name")) {
            meta.setDisplayName(o.get("name").getAsString());
        }
        if (o.has("enchantments")) {
            for(Map.Entry<String, JsonElement> e : o.getAsJsonObject("enchantments").entrySet()) {;
                meta.addEnchant(
                    Enchantment.getByName(e.getKey()),
                    e.getValue().getAsInt(),
                    false);
            }
        }
        if (o.has("lore")) {
            JsonArray jlore = o.getAsJsonArray("lore");
            ArrayList<String> lore = new ArrayList<>();
            for(int i = 0; i < jlore.size(); ++i) {
                lore.add(jlore.get(i).getAsString());
            }
            meta.setLore(lore);
        }
        if (o.has("flags")) {
            JsonArray jflags = o.getAsJsonArray("flags");
            for(int i = 0; i < jflags.size(); ++i) {
                meta.addItemFlags(ItemFlag.valueOf(jflags.get(i).getAsString()));
            }
        }
        return meta;
    }
    
    private static BaxEntry fromJsonVer2_0(JsonObject o) {
        BaxEntry entry = new BaxEntry();
        ItemStack stack = new ItemStack(Material.getMaterial(o.get("id").getAsString()), 1, o.get("damage").getAsShort());
        if (o.has("enchantments")) {
            for(Map.Entry<String, JsonElement> e : o.getAsJsonObject("enchantments").entrySet()) {;
                stack.addEnchantment(Enchantment.getByName(e.getKey()), e.getValue().getAsInt());
            }
        }
        entry.setItem(stack);
        entry.retailPrice = o.get("retail").getAsDouble();
        entry.refundPrice = o.get("refund").getAsDouble();
        int num = o.get("quantity").getAsInt();
        if (num < 0) {
            entry.infinite = true;
        }
        else {
            entry.setAmount(num);
        }
        return entry;
    }
    
    private static BaxEntry fromJsonVer0(JsonObject o) {
        BaxEntry entry = new BaxEntry();
        ItemStack stack = new ItemStack(o.get("id").getAsInt(), 0, o.get("damage").getAsShort());
        if (o.has("enchantments")) {
            for(Map.Entry<String, JsonElement> e : o.getAsJsonObject("enchantments").entrySet()) {;
                stack.addEnchantment(
                    Enchantment.getById(Integer.parseInt(e.getKey())),
                    e.getValue().getAsInt());
            }
        }
        entry.setItem(stack);
        entry.retailPrice = o.get("retail").getAsDouble();
        entry.refundPrice = o.get("refund").getAsDouble();
        int num = o.get("quantity").getAsInt();
        if (num < 0) {
            entry.infinite = true;
        }
        else {
            entry.setAmount(num);
        }
        return entry;
    }
}
