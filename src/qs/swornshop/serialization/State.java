package qs.swornshop.serialization;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import qs.swornshop.Shop;
import qs.swornshop.ShopEntry;
import qs.swornshop.notification.Notification;

/**
 * State saves the whole state of the Shops plugin in a serializable class.
 */
public class State implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * The shops map
	 */
	public HashMap<BlockLocation, Shop> shops = new HashMap<BlockLocation, Shop>();
	/**
	 * The notifications map
	 */
	public HashMap<String, ArrayDeque<Notification>> pending;

	/**
	 * Converts this state's shops back to the map in which it is 
	 * stored during runtime.
	 * @return the shop map
	 */
	public HashMap<Location, Shop> getShops() {
		HashMap<Location, Shop> deserialized = new HashMap<Location, Shop>();
		for (Entry<BlockLocation, Shop> entry : shops.entrySet()) {
			Location loc = entry.getKey().toLocation();
		    Shop shop = entry.getValue();
		    shop.location = loc;
		    Map<org.bukkit.enchantments.Enchantment, Integer> enchantments;
		    for (ShopEntry e : shop.inventory) {
		    	e.item = new ItemStack(e.itemID, e.quantity, (short) e.itemDamage);
		    	enchantments = new HashMap<Enchantment, Integer>(e.enchantments.size());
		    	for (Entry<Integer, Integer> en : e.enchantments.entrySet())
		    		enchantments.put(Enchantment.getById(en.getKey()), en.getValue());
		    	e.item.addEnchantments(enchantments);
		    }
		   deserialized.put(loc, shop);
		}
		return deserialized;
	}

}
