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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.MathUtil;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.items.ItemUtil;
import org.tbax.baxshops.serialization.states.State_00100;
import org.tbax.baxshops.serialization.states.State_00200;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ShopEntry implements Serializable
{
    private static final long serialVersionUID = 1L;
    public double retailPrice;
    public double refundPrice;
    public int quantity;
    public int itemID;
    public int itemDamage;
    public HashMap<Integer, Integer> enchantments = new HashMap<>();

    public ShopEntry() {
        this.retailPrice = -1.0;
        this.refundPrice = -1.0;
    }

    public ShopEntry(JsonObject o)
    {
        itemID = o.get("id").getAsInt();
        itemDamage = o.get("damage").getAsShort();
        quantity = o.get("quantity").getAsInt();
        retailPrice = MathUtil.roundedDouble(o.get("retail").getAsDouble());
        refundPrice = MathUtil.roundedDouble(o.get("refund").getAsDouble());
        if (o.has("enchantments")) {
            for (final Map.Entry<String, JsonElement> entry : o.get("enchantments").getAsJsonObject().entrySet()) {
                enchantments.put(Integer.parseInt(entry.getKey()), entry.getValue().getAsInt());
            }
        }
    }

    public ItemStack toItemStack()
    {
        ItemStack stack = ItemUtil.fromItemId(itemID, (short)itemDamage);
        if (enchantments != null) {
            for (Map.Entry<Integer, Integer> entry : enchantments.entrySet()) {
                Enchantment e = ItemUtil.getLegacyEnchantment(entry.getKey());
                if (e == null) {
                    ShopPlugin.logWarning("Unknown enchantment id " + entry.getKey() + ". This will not be added.");
                }
                else {
                    stack.addUnsafeEnchantment(e, entry.getValue());
                }
            }
        }
        stack.setAmount(1);
        return stack;
    }

    public BaxEntry modernize(State_00100 state00100)
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

    public BaxEntry modernize(State_00200 state00200)
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
