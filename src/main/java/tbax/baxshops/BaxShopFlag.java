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
package tbax.baxshops;

public final class BaxShopFlag
{
    public static final int NONE = 0;              // 000000
    public static final int INFINITE = 1<<0;       // 000001
    public static final int SELL_TO_SHOP = 1<<1;   // 000010
    public static final int BUY_REQUESTS = 1<<2;   // 000100
    public static final int SELL_REQUESTS = 1<<3;  // 001000
    public static final int SMART_STACK = 1<<4;    // 010000

    private BaxShopFlag()
    {
    }
    
    public static boolean hasFlag(int n, int flag)
    {
        return (n & flag) == flag;
    }

    public static int setFlag(int n, int flag, boolean value)
    {
        if (value) {
            return setFlag(n, flag);
        }
        else {
            return unsetFlag(n, flag);
        }
    }

    public static int setFlag(int n, int flag)
    {
        return n | flag;
    }

    public static int unsetFlag(int n, int flag)
    {
        return n & (~flag);
    }
}
