/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization;

import org.bukkit.configuration.file.FileConfiguration;
import tbax.baxshops.BaxShop;
import tbax.baxshops.BaxShopFlag;
import tbax.baxshops.ShopPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class StateConversion
{
    private static final Map<Long, UUID> legacyIds = new HashMap<>();

    private StateConversion()
    {
    }

    public static int flagMapToFlag(SafeMap args)
    {
        int flags = BaxShopFlag.NONE;
        if (args.containsKey("buyRequests")) {
            flags = BaxShopFlag.setFlag(flags, BaxShopFlag.BUY_REQUESTS, args.getBoolean("buyRequests", false));
        }
        if (args.containsKey("infinite")) {
            flags = BaxShopFlag.setFlag(flags, BaxShopFlag.INFINITE, args.getBoolean("infinite", false));
        }
        if (args.containsKey("notify")) {
            flags = BaxShopFlag.setFlag(flags, BaxShopFlag.NOTIFY, args.getBoolean("notify", true));
        }
        if (args.containsKey("sellRequests")) {
            flags = BaxShopFlag.setFlag(flags, BaxShopFlag.SELL_REQUESTS, args.getBoolean("sellRequests", true));
        }
        if (args.containsKey("sellToShop")) {
            flags = BaxShopFlag.setFlag(flags, BaxShopFlag.SELL_TO_SHOP, args.getBoolean("sellToShop", false));
        }
        return flags;
    }

    public static StoredData load(StoredData storedData, FileConfiguration state, double ver)
    {
        return storedData;
    }

    public static BaxShop getShop(long legacyId)
    {
        return ShopPlugin.getShop(legacyIds.get(legacyId));
    }
}
