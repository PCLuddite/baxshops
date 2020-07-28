/*
 * Copyright (C) Timothy Baxendale
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package tbax.shops;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.serialization.states.StateLoader_00050;
import org.tbax.baxshops.serialization.states.StateLoader_00100;
import org.tbax.baxshops.serialization.states.StateLoader_00200;
import org.tbax.baxshops.serialization.states.StateLoader_00210;
import tbax.shops.serialization.BlockLocation;

import java.io.Serializable;
import java.util.*;

@Deprecated
public class BaxShop extends Shop implements Serializable
{
    private static final long serialVersionUID = 1L;
    public HashMap<String, Object> flags;

    public int uid = -1;
    public boolean sellToShop = false;
    public boolean notify = true;
    public boolean buyRequests = false;
    public boolean sellRequests = true;
    public Set<Location> locations;
    
    public BaxShop() {
        this.flags = new HashMap<>();
    }

    public BaxShop(StateLoader_00100 stateLoader_00100, int uid, JsonObject o) {
        this.uid = uid;
        this.owner = o.get("owner").getAsString();
        if (o.has("infinite")) {
            this.isInfinite = o.get("infinite").getAsBoolean();
        }
        if (o.has("sellToShop")) {
            this.sellToShop = o.get("sellToShop").getAsBoolean();
        }
        if (o.has("buyRequests")) {
            this.buyRequests = o.get("buyRequests").getAsBoolean();
        }
        if (o.has("sellRequests")) {
            this.sellRequests = o.get("sellRequests").getAsBoolean();
        }
        if (o.has("notify")) {
            this.notify = o.get("notify").getAsBoolean();
        }

        locations = new HashSet<>();
        for (JsonElement jsonElement : o.get("locations").getAsJsonArray()) {
            JsonObject jsonLoc = jsonElement.getAsJsonObject();
            locations.add(new Location(Bukkit.getServer().getWorld(jsonLoc.get("world").getAsString()),
                    jsonLoc.get("x").getAsInt(),
                    jsonLoc.get("y").getAsInt(),
                    jsonLoc.get("z").getAsInt())
            );
        }

        for (JsonElement jsonElement : o.get("entries").getAsJsonArray()) {
            if (stateLoader_00100 instanceof StateLoader_00210) {
                inventory.add(new ShopEntry((StateLoader_00210)stateLoader_00100, jsonElement.getAsJsonObject()));
            }
            else if (stateLoader_00100 instanceof StateLoader_00200) {
                inventory.add(new ShopEntry((StateLoader_00200)stateLoader_00100, jsonElement.getAsJsonObject()));
            }
            else {
                inventory.add(new ShopEntry(stateLoader_00100, jsonElement.getAsJsonObject()));
            }
        }
    }

    public Object getOption(final String flagName) {
        if (this.flags.containsKey(flagName)) {
            return this.flags.get(flagName);
        }
        return false;
    }

    public org.tbax.baxshops.BaxShop modernize(StateLoader_00100 stateLoader_00100) {
        org.tbax.baxshops.BaxShop baxShop = new org.tbax.baxshops.BaxShop(locations);
        for (ShopEntry entry : inventory) {
            baxShop.add(entry.update(stateLoader_00100));
        }
        baxShop.setFlagInfinite(isInfinite == null ? false : isInfinite);
        baxShop.setFlagBuyRequests(buyRequests);
        baxShop.setFlagSellToShop(sellToShop);
        baxShop.setFlagSellRequests(sellRequests);
        baxShop.setOwner(stateLoader_00100.getPlayerSafe(null, owner));
        return baxShop;
    }

    public org.tbax.baxshops.BaxShop update(StateLoader_00050 stateLoader_00050)
    {
        org.tbax.baxshops.BaxShop baxShop = super.update(stateLoader_00050);
        Object buyRequests = flags.get("buy_request"),
               sellRequests = flags.get("sell_request"),
               sellToShop = flags.get("sell_to_shop");

        if (buyRequests instanceof Boolean) baxShop.setFlagBuyRequests((Boolean)buyRequests);
        if (sellRequests instanceof Boolean) baxShop.setFlagSellRequests((Boolean)sellRequests);
        if (sellToShop instanceof Boolean) baxShop.setFlagSellToShop((Boolean)sellToShop);

        if ((Boolean)getOption("ignore_damage")) {
            ShopPlugin.logWarning(String.format(
                    "Shop %s uses the 'ignore_damage' flag which is not supported. This flag will be removed.",
                    baxShop.getId().toString())
            );
        }


        if (getOption("ref") instanceof BaxShop) {
            org.tbax.baxshops.BaxShop mainShop = stateLoader_00050.getBaxShop((BaxShop)getOption("ref"));
            ShopPlugin.logWarning(String.format("Shop %s is a reference to %s. All of its locations will be removed and replaced with the main shop.",
                    baxShop.getId().toString(),
                    mainShop.getId().toString()
            ));
            for (Location loc : baxShop.getLocations()) {
                mainShop.addLocation(loc);
                baxShop.removeLocation(loc);
            }
        }
        else {
            loadLocations(baxShop);
        }

        return baxShop;
    }

    public void loadLocations(org.tbax.baxshops.BaxShop baxShop) {
        baxShop.addLocation(location);
        if (!(getOption("ref_list") instanceof List)) {
            return;
        }
        List<?> refList = (List<?>)getOption("ref_list");
        for (Object o : refList) {
            if (o instanceof BlockLocation) {
                baxShop.addLocation(((BlockLocation)o).toLocation());
            }
            else {
                ShopPlugin.logWarning(String.format("Unable to convert location of type %s for shop %s",
                        o.getClass().getName(),
                        baxShop.getId().toString()
                ));
            }
        }
    }

    public Iterable<? extends Location> getLocations()
    {
        return Collections.unmodifiableSet(locations);
    }
}
