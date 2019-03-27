/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization.states;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxShop;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.notification.NoteSet;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.Collection;

public interface StateLoader
{
    @NotNull Collection<BaxShop> buildShopList(@NotNull FileConfiguration config);
    @NotNull Collection<StoredPlayer> buildPlayerList(@NotNull FileConfiguration config);
    @NotNull Collection<NoteSet> buildNotificationList(@NotNull FileConfiguration config);

    @NotNull ShopPlugin getPlugin();

    default StoredData load(@NotNull FileConfiguration config)
    {
        StoredData storedData = new StoredData(getPlugin());

        Collection<BaxShop> shops = buildShopList(config);
        Collection<StoredPlayer> players = buildPlayerList(config);
        Collection<NoteSet> notes = buildNotificationList(config);

        for (StoredPlayer player : players) {
            storedData.players.put(player);
        }

        for (BaxShop shop : shops) {
            storedData.shops.put(shop.getId(), shop);
            for(Location location : shop.getLocations()) {
                storedData.locations.put(location, shop.getId());
            }
        }

        for (NoteSet noteSet : notes) {
            storedData.pending.put(noteSet.getRecipient(), noteSet.getNotifications());
        }

        return storedData;
    }
}
