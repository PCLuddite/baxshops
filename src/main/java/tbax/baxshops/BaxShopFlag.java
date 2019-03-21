/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops;

public final class BaxShopFlag
{
    public static final int NONE = 0;              // 000000
    public static final int INFINITE = 1<<1;       // 000001
    public static final int SELL_TO_SHOP = 1<<2;   // 000010
    public static final int NOTIFY = 1<<3;         // 000100
    public static final int BUY_REQUESTS = 1<<4;   // 001000
    public static final int SELL_REQUESTS = 1<<5;  // 010000

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
