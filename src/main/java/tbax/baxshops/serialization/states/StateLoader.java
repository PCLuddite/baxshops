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
    @NotNull Collection<BaxShop> buildShops(@NotNull FileConfiguration state);
    @NotNull Collection<StoredPlayer> buildPlayers(@NotNull FileConfiguration state);
    @NotNull Collection<NoteSet> buildNotifications(@NotNull FileConfiguration state);

    @NotNull ShopPlugin getPlugin();

    default StoredData loadState(@NotNull FileConfiguration state)
    {
        StoredData storedData = new StoredData(getPlugin());

        Collection<BaxShop> shops = buildShops(state);
        Collection<StoredPlayer> players = buildPlayers(state);
        Collection<NoteSet> notes = buildNotifications(state);

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
