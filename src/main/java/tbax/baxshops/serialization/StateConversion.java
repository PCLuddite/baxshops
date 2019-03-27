/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization;

import org.bukkit.OfflinePlayer;
import tbax.baxshops.BaxShopFlag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class StateConversion
{
    private static final Map<Long, UUID> legacyIds = new HashMap<>();
    private static final PlayerMap players = new PlayerMap();

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

    public static UUID getShopId(long legacyId)
    {
        return legacyIds.get(legacyId);
    }

    public static OfflinePlayer getPlayer(String playerName)
    {
        if (playerName == null)
            return StoredPlayer.ERROR;
        return players.get(playerName).get(0);
    }

    public static UUID getPlayerId(String playerName)
    {
        if (playerName == null)
            return StoredPlayer.ERROR_UUID;
        return players.get(playerName).get(0).getUniqueId();
    }

    public static Collection<StoredPlayer> getPlayers()
    {
        return players.values();
    }

    public static void addLegacyShop(long legacyId, UUID id)
    {
        legacyIds.put(legacyId, id);
    }

    public static void clearMaps()
    {
        legacyIds.clear();
        players.clear();
    }
}
