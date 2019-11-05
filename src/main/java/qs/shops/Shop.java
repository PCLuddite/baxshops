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
package qs.shops;

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
	
	public Shop() { }
	/**
	 * The username of the player who owns this shop
	 */
	public String owner;
	
	public Boolean isInfinite;
	
	/**
	 * This shop's inventory
	 */
	public ArrayList<ShopEntry> inventory = new ArrayList<ShopEntry>();
	/**
	 * The block location of this shop
	 */
	public transient Location location;
	
	private int ceil(float x) {
		return (int) Math.ceil(x);
	}
	
	
	/**
	 * Gets the number of pages in this shop's inventory.
	 * @return the number of pages
	 */
	public int getPages() {
		return ceil((float) inventory.size() / ITEMS_PER_PAGE);
	}
	
	/**
	 * Gets the number of items in this shop's inventory.
	 * @return the number of items
	 */
	public int getInventorySize() {
		return inventory.size();
	}
	
	/**
	 * Gets the entry at the given index in this shop's inventory.
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
	 * Find an entry for an item in this shop's inventory.
	 * @param id the item's ID
	 * @param damage the item's damage value (durability)
	 * @return the item's entry, or null
	 */
	public ShopEntry findEntry(int id, int damage) {
		for (ShopEntry e : inventory) {
			if (e.itemID == id &&
					e.itemDamage == damage)
				return e;
		}
		return null;
	}
}
