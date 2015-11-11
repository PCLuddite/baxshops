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
package tbax.baxshops.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;

/**
 *
 * @author tbaxendale
 */
public final class BaxShopSerializer
{
    public static JsonObject serialize(double version, BaxShop shop) {
        JsonObject o = new JsonObject();
        o.addProperty("owner", shop.owner);
        // default false
        addIfTrue(o, "infinite", shop.infinite);
        addIfTrue(o, "sellToShop", shop.sellToShop);
        addIfTrue(o, "buyRequests", shop.buyRequests);
        // default true
        addIfFalse(o, "sellRequests", shop.sellRequests);
        addIfFalse(o, "notify", shop.notify);
        
        JsonArray aLocs = new JsonArray();
        for(Location loc : shop.locations) {
            aLocs.add(toJson(loc));
        }
        o.add("locations", aLocs);
        
        JsonArray aItems = new JsonArray();
        for(BaxEntry e : shop.inventory) {
            aItems.add(BaxEntrySerializer.serialize(version, e));
        }
        o.add("entries", aItems);
        
        return o;
    }

    private static void addIfTrue(JsonObject o, String key, boolean value) {
	if (value) {
            o.addProperty(key, value);
	}
    }
    
    private static void addIfFalse(JsonObject o, String key, boolean value) {
	if (!value) {
            o.addProperty(key, value);
	}
    }

    private static JsonElement toJson(Location loc) {
        JsonObject o = new JsonObject();
        o.addProperty("world", loc.getWorld().getName());
        o.addProperty("x", loc.getBlockX());
        o.addProperty("y", loc.getBlockY());
        o.addProperty("z", loc.getBlockZ());
        return o;
    }
}
