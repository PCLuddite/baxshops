/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization;

import tbax.baxshops.BaxShopFlag;

import java.util.Map;

public final class StateConversion
{
    private StateConversion()
    {
    }

    public static int flagMapToFlag(Map<String, Object> args)
    {
        int flags = BaxShopFlag.NONE;
        if (args.containsKey("buyRequests")) {
            flags = BaxShopFlag.setFlag(flags, BaxShopFlag.BUY_REQUESTS, (boolean)args.get("buyRequests"));
        }
        if (args.containsKey("infinite")) {
            flags = BaxShopFlag.setFlag(flags, BaxShopFlag.INFINITE, (boolean)args.get("infinite"));
        }
        if (args.containsKey("notify")) {
            flags = BaxShopFlag.setFlag(flags, BaxShopFlag.NOTIFY, (boolean)args.get("notify"));
        }
        if (args.containsKey("sellRequests")) {
            flags = BaxShopFlag.setFlag(flags, BaxShopFlag.SELL_REQUESTS, (boolean)args.get("sellRequests"));
        }
        if (args.containsKey("sellToShop")) {
            flags = BaxShopFlag.setFlag(flags, BaxShopFlag.SELL_TO_SHOP, (boolean)args.get("sellToShop"));
        }
        return flags;
    }
}
