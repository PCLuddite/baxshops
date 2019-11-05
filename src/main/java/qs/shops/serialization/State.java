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
