package qs.shops.serialization;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;

import qs.shops.Shop;
import qs.shops.notification.Notification;

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
		    Shop shop = entry.getValue();
		    deserialized.put(shop.location = entry.getKey().toLocation(), shop);
		}
		return deserialized;
	}

}
