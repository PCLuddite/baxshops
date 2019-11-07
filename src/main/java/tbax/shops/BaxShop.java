/*
 * Copyright (C) 2013-2019 Timothy Baxendale
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

import org.bukkit.Location;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.serialization.states.State_00100;
import tbax.shops.serialization.BlockLocation;

import java.io.*;
import java.util.*;

public class BaxShop extends Shop implements Serializable
{
    private static final long serialVersionUID = 1L;
    public HashMap<String, Object> flags;
    
    public BaxShop() {
        this.flags = new HashMap<>();
    }

    public Object getOption(final String flagName) {
        if (this.flags.containsKey(flagName)) {
            return this.flags.get(flagName);
        }
        return false;
    }

    public Object setOption(final String flagName, final Object option) {
        return this.flags.put(flagName, option);
    }

    public org.tbax.baxshops.BaxShop modernize(State_00100 state_00100)
    {
        org.tbax.baxshops.BaxShop baxShop = super.modernize(state_00100);
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
            org.tbax.baxshops.BaxShop mainShop = state_00100.registerShop((BaxShop)getOption("ref"));
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
}
