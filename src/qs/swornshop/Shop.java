package qs.swornshop;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * A Shop represents a user's shop and its inventory of items.
 */
public class Shop {
	public Shop() { }
	/**
	 * The username of the player who owns this shop
	 */
	public String owner;
	/**
	 * This shop's inventory
	 */
	public HashMap<ItemStack, ShopEntry> inventory = new HashMap<ItemStack, ShopEntry>();
	/**
	 * The block location of this shop
	 */
	public Location location;
}
