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

import org.bukkit.Location;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.internal.serialization.states.StateLoader_00000;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A Shop represents a user's shop and its inventory of items.
 */
@Deprecated
public class Shop implements Serializable {
	private static final long serialVersionUID = 1L;
	
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

	public BaxShop modernize(StateLoader_00000 state00000)
	{
		BaxShop baxShop = new BaxShop(location);
		baxShop.setFlagInfinite(isInfinite);
		baxShop.setOwner(state00000.registerPlayer(owner));
		for(ShopEntry entry : inventory) {
			baxShop.add(entry.modernize());
		}
		return baxShop;
	}
}
