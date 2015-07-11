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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * A Shop represents a user's shop and its inventory of items.
 */
public class Shop implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * The number of items in each page of a shop's listing
     */
    public static final int ITEMS_PER_PAGE = 7;

    public Shop() {
    }
    /**
     * The username of the player who owns this shop
     */
    public String owner;

    public Boolean isInfinite;

    /**
     * This shop's inventory
     */
    public ArrayList<ShopEntry> inventory = new ArrayList();
    /**
     * The block location of this shop
     */
    public transient Location location;

    private int ceil(double x) {
        return (int) Math.ceil(x);
    }

    /**
     * Gets the number of pages in this shop's inventory.
     *
     * @return the number of pages
     */
    public int getPages() {
        return ceil((double) inventory.size() / ITEMS_PER_PAGE);
    }

    /**
     * Gets the number of items in this shop's inventory.
     *
     * @return the number of items
     */
    public int getInventorySize() {
        return inventory.size();
    }

    /**
     * Gets the entry at the given index in this shop's inventory.
     *
     * @return the shop entry
     */
    public ShopEntry getEntryAt(int index) {
        return inventory.get(index);
    }

    /**
     * Add an item to this shop's inventory.
     */
    public void addEntry(ShopEntry entry) {
        inventory.add(entry);
    }

    /**
     * Checks if this shop's inventory contains an item.
     *
     * @param stack the item to check for
     * @return whether the shop contains the item
     */
    public boolean containsItem(ItemStack stack) {
        //return containsItem(stack.getTypeId(), stack.getDurability());
        for (ShopEntry e : inventory) {
            if (e.itemID == stack.getTypeId()
                    && e.itemDamage == stack.getDurability()) {

                HashMap<Integer, Integer> compare = new HashMap<Integer, Integer>();
                for (Map.Entry<Enchantment, Integer> entry : stack.getEnchantments().entrySet()) {
                    compare.put(entry.getKey().getId(), entry.getValue());
                }
                if (compare.equals(e.enchantments)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if this shop's inventory contains an item.
     *
     * @param item the item's ID
     * @param damage the item's damage value (durability)
     * @return whether the shop contains the item
     */
    public boolean containsItem(int id, int damage) {
        for (ShopEntry e : inventory) {
            if (e.itemID == id
                    && e.itemDamage == damage) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find an entry for an item in this shop's inventory.
     *
     * @param stack the item to find
     * @return the item's entry, or null
     */
    public ShopEntry findEntry(ItemStack stack) {
        for (ShopEntry e : inventory) {
            if (e.itemID == stack.getTypeId()
                    && e.itemDamage == stack.getDurability()) {
                HashMap<Integer, Integer> compare = new HashMap<Integer, Integer>();
                for (Map.Entry<Enchantment, Integer> entry : stack.getEnchantments().entrySet()) {
                    compare.put(entry.getKey().getId(), entry.getValue());
                }
                if (compare.equals(e.enchantments)) {
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * Find an entry for an item in this shop's inventory.
     *
     * @param id the item's ID
     * @param damage the item's damage value (durability)
     * @return the item's entry, or null
     */
    public ShopEntry findEntry(int id, int damage) {
        for (ShopEntry e : inventory) {
            if (e.itemID == id
                    && e.itemDamage == damage) {
                return e;
            }
        }
        return null;
    }

    /*public BaxShop toBaxShop() {
     BaxShop baxShop = new BaxShop();
     baxShop.isInfinite = isInfinite;
     baxShop.location = location;
     baxShop.owner = owner;
        
     for(ShopEntry entry : inventory) {
     BaxShopEntry baxEntry = new BaxShopEntry();
     baxEntry.enchantments = entry.enchantments;
     baxEntry.itemDamage = entry.itemDamage;
     baxEntry.itemID = entry.itemID;
     baxEntry.quantity = entry.quantity;
     baxEntry.refundPrice = entry.refundPrice;
     baxEntry.retailPrice = entry.retailPrice;
     baxShop.addEntry(baxEntry);
     }
        
     return baxShop;
     }*/
}
