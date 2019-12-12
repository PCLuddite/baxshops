/*
 * Copyright © 2012 Nathan Dinsmore and Sam Lazarus
 * Modifications Copyright © Timothy Baxendale
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package tbax.shops;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.MathUtil;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.internal.items.ItemUtil;
import org.tbax.baxshops.internal.serialization.states.StateLoader_00050;
import org.tbax.baxshops.internal.serialization.states.StateLoader_00100;
import org.tbax.baxshops.internal.serialization.states.StateLoader_00200;
import org.tbax.baxshops.internal.serialization.states.StateLoader_00210;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class ShopEntry implements Serializable
{
    private static final long serialVersionUID = 1L;
    public double retailPrice;
    public double refundPrice;
    public int quantity;
    public int itemID;
    public int itemDamage;
    public HashMap<Integer, Integer> enchantments = new HashMap<>();
    private ItemStack stack;

    public ShopEntry() {
        this.retailPrice = -1.0;
        this.refundPrice = -1.0;
    }

    public ShopEntry(StateLoader_00100 state00200, JsonObject o)
    {
        stack = ItemUtil.fromItemId(o.get("id").getAsInt(), o.get("damage").getAsShort());
        quantity = o.get("quantity").getAsInt();
        retailPrice = MathUtil.roundedDouble(o.get("retail").getAsDouble());
        refundPrice = MathUtil.roundedDouble(o.get("refund").getAsDouble());
        if (o.has("enchantments")) {
            for (Map.Entry<String, JsonElement> entry : o.get("enchantments").getAsJsonObject().entrySet()) {
                Enchantment e = ItemUtil.getLegacyEnchantment(Integer.parseInt(entry.getKey()));
                if (e == null) {
                    ShopPlugin.logWarning("Unknown enchantment id " + entry.getKey() + ". This will not be added.");
                }
                else {
                    stack.addUnsafeEnchantment(e, entry.getValue().getAsInt());
                }
            }
        }
    }

    public ShopEntry(StateLoader_00200 state00205, JsonObject o)
    {
        stack = new ItemStack(Material.getMaterial(o.get("id").getAsString()), 1, o.get("damage").getAsShort());
        if (o.has("enchantments")) {
            for (Map.Entry<String, JsonElement> e : o.getAsJsonObject("enchantments").entrySet()) {
                Enchantment enchantment = Enchantment.getByName(e.getKey());
                if (enchantment == null) {
                    ShopPlugin.logWarning("Unknown enchantment id " + e.getKey() + ". This will not be added.");
                }
                else {
                    stack.addEnchantment(enchantment, e.getValue().getAsInt());
                }
            }
        }
        retailPrice = MathUtil.roundedDouble(o.get("retail").getAsDouble());
        refundPrice = MathUtil.roundedDouble(o.get("refund").getAsDouble());
        quantity = o.get("quantity").getAsInt();
    }

    public ShopEntry(StateLoader_00210 state00210, JsonObject o)
    {
        stack = new ItemStack(Material.getMaterial(o.get("id").getAsString()), 1, o.get("damage").getAsShort());
        if (o.has("enchantments")) {
            for (final Map.Entry<String, JsonElement> e : o.getAsJsonObject("enchantments").entrySet()) {
                stack.addEnchantment(Enchantment.getByName((String)e.getKey()), e.getValue().getAsInt());
            }
        }
        retailPrice = o.get("retail").getAsDouble();
        refundPrice = o.get("refund").getAsDouble();
        quantity = o.get("quantity").getAsInt();

        if (o.has("meta")) {
            ItemMeta meta = stack.getItemMeta();
            o = o.getAsJsonObject("meta");

            if (o.has("name")) {
                meta.setDisplayName(o.get("name").getAsString());
            }
            if (o.has("enchantments")) {
                for (final Map.Entry<String, JsonElement> e : o.getAsJsonObject("enchantments").entrySet()) {
                    meta.addEnchant(Enchantment.getByName(e.getKey()), e.getValue().getAsInt(), false);
                }
            }
            if (o.has("lore")) {
                JsonArray jlore = o.getAsJsonArray("lore");
                List<String> lore = new ArrayList<String>();
                for (int i = 0; i < jlore.size(); ++i) {
                    lore.add(jlore.get(i).getAsString());
                }
                meta.setLore(lore);
            }
            if (o.has("flags")) {
                final JsonArray jflags = o.getAsJsonArray("flags");
                for (int j = 0; j < jflags.size(); ++j) {
                    meta.addItemFlags(ItemFlag.valueOf(jflags.get(j).getAsString()));
                }
            }
        }
    }

    public ItemStack toItemStack()
    {
        if (stack != null) return stack.clone();
        stack = ItemUtil.fromItemId(itemID, (short)itemDamage);
        stack.setAmount(quantity);
        for (Map.Entry<Integer, Integer> entry : enchantments.entrySet()) {
            Enchantment e = ItemUtil.getLegacyEnchantment(entry.getKey());
            if (e == null) {
                ShopPlugin.logWarning("Unknown enchantment id " + entry.getKey() + ". This will not be added.");
            }
            else {
                stack.addUnsafeEnchantment(e, entry.getValue());
            }
        }
        return stack;
    }

    public BaxEntry modernize(StateLoader_00050 state00100)
    {
        BaxEntry baxEntry = new BaxEntry();
        baxEntry.setRefundPrice(MathUtil.roundedDouble(refundPrice));
        baxEntry.setRetailPrice(MathUtil.roundedDouble(retailPrice));
        baxEntry.canBuy(true);
        if (refundPrice >= 0) baxEntry.canSell(true);
        baxEntry.setAmount(quantity);
        baxEntry.setItem(toItemStack());
        return baxEntry;
    }

    public BaxEntry modernize(StateLoader_00100 state00200)
    {
        BaxEntry baxEntry = new BaxEntry();
        baxEntry.setRefundPrice(refundPrice);
        baxEntry.setRetailPrice(retailPrice);
        baxEntry.canBuy(true);
        if (refundPrice >= 0) baxEntry.canSell(true);
        baxEntry.setAmount(quantity);
        baxEntry.setItem(toItemStack());
        return baxEntry;
    }
}
