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
package tbax.shops;

import tbax.baxshops.Main;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * A ShopEntry represents a single item in the inventory of a shop,
 * with a retail (buy) price and a refund (sell) price. If the item's
 * refund price is -1, the item cannot be sold to the shop.
 */
public class ShopEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * The price per unit to buy this item from the store
     */
    public double retailPrice = -1;
    /**
     * The price per unit to sell this item to the store
     */
    public double refundPrice = -1;
    /**
     * The item's quantity
     */
    public int quantity;
    /**
     * The item's ID
     */
    public int itemID;
    /**
     * The item's damage value (durability)
     */
    public int itemDamage;

    /**
     * The item's enchantments
     */
    public HashMap<Integer, Integer> enchantments = new HashMap<>();

    /**
     * Sets the item associated with this shop entry.
     * @param item an ItemStack
     */
    public void setItem(ItemStack item) {
        this.quantity = item.getAmount();
        this.itemID = item.getTypeId();
        this.itemDamage = item.getDurability();
        this.extractEnchantments(item);
    }

    protected void extractEnchantments(ItemStack item) {
        for (Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            enchantments.put(entry.getKey().getId(), entry.getValue());
        }
    }

    /**
     * Sets the amount of the item stack associated with this entry.
     * @param amount the quantity of the item
     */
    public void setAmount(int amount) {
        this.quantity = amount;
    }

    /**
     * Converts this entry to an item stack.
     * @return an item stack
     */
    public ItemStack toItemStack() {
        ItemStack i = new ItemStack(itemID, quantity, (short) itemDamage);
        for (Entry<Integer, Integer> entry : enchantments.entrySet()) {
            i.addUnsafeEnchantment(Enchantment.getById(entry.getKey()), entry.getValue());
        }
        return i;
    }

    public String toString(int index) {
        return null;
    }
}
