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
package tbax.shops.serialization;

import java.io.*;
import tbax.shops.*;
import tbax.shops.notification.*;
import org.bukkit.*;
import java.util.*;

@Deprecated
public class State2 implements Serializable
{
    private static final long serialVersionUID = 1L;
    public HashMap<BlockLocation, BaxShop> shops;
    public HashMap<String, ArrayDeque<Notification>> pending;

    public State2() {
        this.shops = new HashMap<>();
    }

    public HashMap<Location, BaxShop> getShops() {
        final HashMap<Location, BaxShop> deserialized = new HashMap<Location, BaxShop>();
        for (final Map.Entry<BlockLocation, BaxShop> entry : this.shops.entrySet()) {
            final BaxShop shop = entry.getValue();
            deserialized.put(shop.location = entry.getKey().toLocation(), shop);
        }
        return deserialized;
    }
}
