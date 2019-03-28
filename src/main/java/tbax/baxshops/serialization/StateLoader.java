/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxShop;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.notification.NoteSet;

import java.util.Collection;
import java.util.UUID;

public interface StateLoader
{
    @NotNull Collection<BaxShop> buildShops(@NotNull FileConfiguration state);
    @NotNull Collection<StoredPlayer> buildPlayers(@NotNull FileConfiguration state);
    @NotNull Collection<NoteSet> buildNotifications(@NotNull FileConfiguration state);

    @NotNull ShopPlugin getPlugin();

    default @NotNull Configuration loadConfig(@NotNull FileConfiguration config)
    {
        Configuration ret = new Configuration();
        ret.setBackups(config.getInt("Backups", ret.getBackups()));
        ret.setLogNotes(config.getBoolean("LogNotes", ret.isLogNotes()));
        ret.setXpConvert(config.getDouble("XPConvert", ret.getXpConvert()));
        ret.setDeathTaxEnabled(config.getBoolean("DeathTax.Enabled", ret.isDeathTaxEnabled()));
        ret.setDeathTaxGoesTo(config.getString("DeathTax.GoesTo", ret.getDeathTaxGoesTo()));
        ret.setDeathTaxPercentage(config.getDouble("DeathTax.Percentage", ret.getDeathTaxPercentage()));
        ret.setDeathTaxMinimum(config.getDouble("DeathTax.Minimum", ret.getDeathTaxMinimum()));
        return ret;
    }

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
