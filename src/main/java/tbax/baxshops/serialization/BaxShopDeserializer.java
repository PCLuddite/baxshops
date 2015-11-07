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
import com.google.gson.JsonObject;
import org.bukkit.Location;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Main;

/**
 *
 * @author tbaxendale
 */
public class BaxShopDeserializer
{
    public static BaxShop deserialize(double version, int uid, JsonObject o) {
        BaxShop shop = new BaxShop();
        shop.uid = uid;
        shop.owner = o.get("owner").getAsString();
        if (o.has("infinite")) {
            shop.infinite = o.get("infinite").getAsBoolean();
        }
        if (o.has("sellToShop")) {
            shop.sellToShop = o.get("sellToShop").getAsBoolean();
        }
        if (o.has("buyRequests")) {
            shop.buyRequests = o.get("buyRequests").getAsBoolean();
        }
        if (o.has("sellRequests")) {
            shop.sellRequests = o.get("sellRequests").getAsBoolean();
        }
        if (o.has("notify")) {
            shop.notify = o.get("notify").getAsBoolean();
        }
        loadLocations(shop, o.get("locations").getAsJsonArray());
        loadEntries(version, shop, o.get("entries").getAsJsonArray());
        return shop;
    }
    
    private static void loadLocations(BaxShop shop, JsonArray a) {
        for(int i = 0; i < a.size(); ++i) {
            Location loc = getLocFromJson(a.get(i).getAsJsonObject());
            shop.locations.add(loc);
        }
    }
    
    private static void loadEntries(double version, BaxShop shop, JsonArray a) {
        for(int i = 0; i < a.size(); ++i) {
            BaxEntry entry = BaxEntryDeserializer.deserialize(version, a.get(i).getAsJsonObject());
            shop.inventory.add(entry);
        }
    }
    
    private static Location getLocFromJson(JsonObject o) {
        return new Location(
                Main.instance.getServer().getWorld(o.get("world").getAsString()),
                o.get("x").getAsInt(),
                o.get("y").getAsInt(),
                o.get("z").getAsInt()
        );
    }
}
