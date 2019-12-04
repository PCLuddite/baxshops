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
package tbax.shops;

import org.bukkit.Location;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.serialization.internal.states.StateLoader_00100;

import java.io.Serializable;
import java.util.ArrayList;

public class Shop implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final int ITEMS_PER_PAGE = 7;
    public String owner;
    public Boolean isInfinite;
    public ArrayList<ShopEntry> inventory;
    public transient Location location;

    public Shop() {
        this.inventory = new ArrayList<>();
    }

    public BaxShop modernize(StateLoader_00100 stateLoader_00100)
    {
        org.tbax.baxshops.BaxShop baxShop = new org.tbax.baxshops.BaxShop(location);
        baxShop.setFlagInfinite(isInfinite == null ? false : isInfinite);
        baxShop.setOwner(stateLoader_00100.registerPlayer(owner));
        for(ShopEntry entry : inventory) {
            baxShop.add(entry.modernize(stateLoader_00100));
        }
        return baxShop;
    }
}
