package qs.swornshop;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import qs.swornshop.notification.Notification;

public class State implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public HashMap<String, Shop> shops = new HashMap<String, Shop>();
	public HashMap<String, ArrayDeque<Notification>> pending = new HashMap<String, ArrayDeque<Notification>>();

	public HashMap<Location, Shop> shopsRecreate(){
		HashMap<Location, Shop> build = new HashMap<Location, Shop>();
		for (Entry<String, Shop> entry : shops.entrySet()) {
			String[] locString = entry.getKey().split(",");
			Location loc = new Location(
					Main.instance.getServer().getWorld(locString[0]),
					Double.parseDouble(locString[1]),
					Double.parseDouble(locString[2]),
					Double.parseDouble(locString[3]));
		    Shop value = entry.getValue();
		    value.location = loc;
		    int i = 0;
		    while(i < value.inventory.size()){
		    	ItemStack item = new ItemStack(value.inventory.get(i).itemID,
		    			value.inventory.get(i).quantity, (short) value.inventory.get(i).itemDamage);
		    	value.inventory.get(i).item = item;
		    	i++;
		    }
		   build.put(loc, value);
		}
		return build;
		
	}

}
