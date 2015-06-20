/* 
 * The MIT License
 *
 * Copyright © 2015 Timothy Baxendale (pcluddite@hotmail.com) and 
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
import org.bukkit.Location;
import tbax.baxshops.Main;

/**
 * A BlockLocation is a Location suitable for serialization.
 */
public class BlockLocation implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a BlockLocation from a {@link Location}.
	 * @param loc the location
	 */
	public BlockLocation(Location loc) {
		x = loc.getBlockX();
		y = loc.getBlockY();
		z = loc.getBlockZ();
		worldName = loc.getWorld().getName();
	}
	
	int x, y, z;
	String worldName;

	/**
	 * Convert this BlockLocation back to a Location
	 * @return
	 */
	public Location toLocation() {
            return new Location(Main.instance.getServer().getWorld(worldName), x, y, z);
	}
}
