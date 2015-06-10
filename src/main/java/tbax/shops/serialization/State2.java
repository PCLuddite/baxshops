package tbax.shops.serialization;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;

import tbax.shops.BaxShop;
import tbax.shops.notification.Notification;

/**
 * State saves the whole state of the Shops plugin in a serializable class.
 */
public class State2 implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * The shops map
	 */
	public HashMap<BlockLocation, BaxShop> shops = new HashMap();
	/**
	 * The notifications map
	 */
	public HashMap<String, ArrayDeque<Notification>> pending;

	/**
	 * Converts this state's shops back to the map in which it is 
	 * stored during runtime.
	 * @return the shop map
	 */
	public HashMap<Location, BaxShop> getShops() {
            HashMap<Location, BaxShop> deserialized = new HashMap();
            for (Entry<BlockLocation, BaxShop> entry : shops.entrySet()) {
                BaxShop shop = entry.getValue();
                deserialized.put(shop.location = entry.getKey().toLocation(), shop);
            }
            return deserialized;
	}

}
