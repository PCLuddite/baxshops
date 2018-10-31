/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops;

public enum BaxShopFlag
{
    None(1<<0),
    Infinite(1<<2),
    SellToShop(1<<3),
    Notify(1<<4),
    BuyRequests(1<<5),
    SellRequests(1<<6);

    private final long flag;

    BaxShopFlag(long flag)
    {
        this.flag = flag;
    }

    public long getFlag()
    {
        return flag;
    }
}
