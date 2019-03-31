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

    default SavedState loadState(@NotNull FileConfiguration state)
    {
        SavedState savedState = new SavedState(getPlugin());

        ShopPlugin.logInfo("Loading shop data...");
        Collection<BaxShop> shops = buildShops(state);
        ShopPlugin.logInfo("Loading notifications...");
        Collection<NoteSet> notes = buildNotifications(state);
        ShopPlugin.logInfo("Loading player data...");
        Collection<StoredPlayer> players = buildPlayers(state);

        for (StoredPlayer player : players) {
            savedState.players.put(player);
        }

        for (BaxShop shop : shops) {
            savedState.shops.put(shop.getId(), shop);
            for(Location location : shop.getLocations()) {
                savedState.locations.put(location, shop.getId());
            }
        }

        for (NoteSet noteSet : notes) {
            savedState.pending.put(noteSet.getRecipient(), noteSet.getNotifications());
        }

        savedState.config = loadConfig(getPlugin().getConfig());
        return savedState;
    }
}
