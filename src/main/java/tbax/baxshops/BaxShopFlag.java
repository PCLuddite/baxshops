/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops;

public final class BaxShopFlag
{
    public static final long NONE = 0;              // 000000
    public static final long INFINITE = 1<<1;       // 000001
    public static final long SELL_TO_SHOP = 1<<2;   // 000010
    public static final long NOTIFY = 1<<3;         // 000100
    public static final long BUY_REQUESTS = 1<<4;   // 001000
    public static final long SELL_REQUESTS = 1<<5;  // 010000

    public static boolean hasFlag(long lng, long flag)
    {
        return (lng & flag) == flag;
    }

    public static long setFlag(long lng, long flag, boolean value)
    {
        if (value) {
            return setFlag(lng, flag);
        }
        else {
            return unsetFlag(lng, flag);
        }
    }

    public static long setFlag(long lng, long flag)
    {
        return lng | flag;
    }

    public static long unsetFlag(long lng, long flag)
    {
        return lng & (~flag);
    }
}
