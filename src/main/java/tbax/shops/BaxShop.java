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

import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.serialization.states.State_00050;

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

    public org.tbax.baxshops.BaxShop modernize(State_00050 state_00050)
    {
        org.tbax.baxshops.BaxShop baxShop = super.modernize(state_00050);
        baxShop.setFlagSellToShop((Boolean)getOption("sell_to_shop"));
        if ((Boolean)getOption("ignore_damage")) {
            ShopPlugin.logWarning("Shop %s uses the 'ignore_damage' flag which is not supported. This flag will be removed.");
        }
        return baxShop;
    }
}
