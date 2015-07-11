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
