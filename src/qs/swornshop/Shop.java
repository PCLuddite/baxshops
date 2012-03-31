package qs.swornshop;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * A Shop represents a user's shop and its inventory of items.
 */
public class Shop {
	public static final int ITEMS_PER_PAGE = 7;
	
	public Shop() { }
	/**
	 * The username of the player who owns this shop
	 */
	public String owner;
	/**
	 * This shop's inventory
	 */
	public ArrayList<ShopEntry> inventory = new ArrayList<ShopEntry>();
	/**
	 * The block location of this shop
	 */
	public Location location;
	
	//private static final int BIG = Integer.MAX_VALUE;
	//private static final float BIGF = BIG;
	private int ceil(float x) {
		//return BIG - (int) (BIGF - x);
		return (int) Math.ceil(x);
	}
	/*private int floor(float x) {
		return (int) x;
	}*/
	
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
	 * Checks if this shop's inventory contains an item.
	 * @param stack the item to check for
	 * @return whether the shop contains the item
	 */
	public boolean containsItem(ItemStack stack) {
		for (ShopEntry e : inventory) {
			if (e.item.getTypeId() == stack.getTypeId() &&
				e.item.getDurability() == stack.getDurability()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Find an entry for an item in this shop's inventory.
	 * @param stack the item to find
	 * @return the item's entry, or null
	 */
	public ShopEntry findEntry(ItemStack stack) {
		for (ShopEntry e : inventory) {
			if (e.item.getTypeId() == stack.getTypeId() &&
				e.item.getDurability() == stack.getDurability()) {
				return e;
			}
		}
		return null;
	}
}
