package qs.swornshop.serialization;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import qs.swornshop.Shop;
import qs.swornshop.ShopEntry;
import qs.swornshop.notification.Notification;

public class State implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public HashMap<BlockLocation, Shop> shops = new HashMap<BlockLocation, Shop>();
	public HashMap<String, ArrayDeque<Notification>> pending;

	public HashMap<Location, Shop> getShops(){
		HashMap<Location, Shop> deserialized = new HashMap<Location, Shop>();
		for (Entry<BlockLocation, Shop> entry : shops.entrySet()) {
			Location loc = entry.getKey().toLocation();
		    Shop shop = entry.getValue();
		    shop.location = loc;
		    for (ShopEntry e : shop.inventory)
		    	e.item = new ItemStack(e.itemID, e.quantity, (short) e.itemDamage);
		   deserialized.put(loc, shop);
		}
		return deserialized;
	}

}
