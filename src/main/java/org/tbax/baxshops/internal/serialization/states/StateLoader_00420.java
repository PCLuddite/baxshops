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
package org.tbax.baxshops.internal.serialization.states;

import org.tbax.baxshops.BaxShopFlag;
import org.tbax.baxshops.internal.ShopPlugin;

public class StateLoader_00420 extends StateLoader_00411
{
    public static final double VERSION = 4.2;

    private static final int NONE = 0;              // 000000
    private static final int INFINITE = 1<<1;       // 000010
    private static final int SELL_TO_SHOP = 1<<2;   // 000100
    private static final int NOTIFY = 1<<3;         // 001000
    private static final int BUY_REQUESTS = 1<<4;   // 010000
    private static final int SELL_REQUESTS = 1<<5;  // 100000

    public StateLoader_00420(ShopPlugin plugin)
    {
        super(plugin);
    }

    public static int convertFlag(int oldFlags)
    {
        int newFlags = BaxShopFlag.NONE;
        if (BaxShopFlag.hasFlag(oldFlags, BUY_REQUESTS)) {
            newFlags = BaxShopFlag.setFlag(newFlags, BaxShopFlag.BUY_REQUESTS);
        }
        if (BaxShopFlag.hasFlag(oldFlags, INFINITE)) {
            newFlags = BaxShopFlag.setFlag(newFlags, BaxShopFlag.INFINITE);
        }
        if (BaxShopFlag.hasFlag(oldFlags, SELL_REQUESTS)) {
            newFlags = BaxShopFlag.setFlag(newFlags, BaxShopFlag.SELL_REQUESTS);
        }
        if (BaxShopFlag.hasFlag(oldFlags, SELL_TO_SHOP)) {
            newFlags = BaxShopFlag.setFlag(newFlags, BaxShopFlag.SELL_TO_SHOP);
        }
        return newFlags;
    }
}
