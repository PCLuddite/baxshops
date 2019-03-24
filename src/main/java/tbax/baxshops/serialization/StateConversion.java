/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import tbax.baxshops.BaxShop;
import tbax.baxshops.BaxShopFlag;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.notification.DeprecatedNote;
import tbax.baxshops.notification.Notification;

import java.util.*;

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

    public static StoredData load(StoredData storedData, FileConfiguration state, double ver)
    {
        if (ver == 3.0) {
            ShopPlugin.getInstance().getLogger().info("Converting state file version 3.0");
            return load30(storedData, state);
        }
        ShopPlugin.getInstance().getLogger().warning("Unknown state file version. Starting from scratch...");
        return storedData;
    }

    private static StoredData load30(StoredData storedData, FileConfiguration state)
    {
        if (state.isList("shops")) {
            for (Object o : state.getList("shops")) {
                if (o instanceof BaxShop) {
                    BaxShop shop = (BaxShop) o;
                    for (Location loc : shop.getLocations()) {
                        storedData.locations.put(loc, shop.getId());
                    }
                    storedData.shops.put(shop.getId(), shop);
                    legacyIds.put(shop.getLegacyId(), shop.getId());
                }
                else {
                    storedData.log.warning("Could not load BaxShop of type " + o.getClass());
                }
            }
        }
        if (state.isConfigurationSection("notes")) {
            for (Map.Entry entry : state.getConfigurationSection("notes").getValues(false).entrySet()) {
                OfflinePlayer player = getPlayer(entry.getKey().toString());
                if (!(entry.getValue() instanceof List)) {
                    storedData.log.warning("Could not load notifications of type " + entry.getValue().getClass());
                }
                List notes = (List)entry.getValue();
                Deque<Notification> pending = new ArrayDeque<>();
                for (Object o : notes) {
                    if (o instanceof Notification) {
                        pending.add((Notification)o);
                    }
                    else if (o instanceof DeprecatedNote) {
                        pending.add(((DeprecatedNote)o).getNewNote());
                    }
                    else {
                        storedData.log.warning("Could not load Notification of type " + entry.getValue().getClass());
                    }
                }
                storedData.pending.put(player.getUniqueId(), pending);
            }
        }
        storedData.players.putAll(players);
        players.clear();
        legacyIds.clear();
        return storedData;
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
}
