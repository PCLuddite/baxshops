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
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.serialization.states.State_00050;

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

    public BaxShop modernize(State_00050 state_00050)
    {
        org.tbax.baxshops.BaxShop baxShop = new org.tbax.baxshops.BaxShop(location);
        baxShop.setFlagInfinite(isInfinite);
        baxShop.setOwner(state_00050.registerPlayer(owner));
        for(ShopEntry entry : inventory) {
            baxShop.add(entry.modernize(state_00050));
        }
        return baxShop;
    }
}
