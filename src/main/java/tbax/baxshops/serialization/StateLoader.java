/*
 * Copyright (C) 2013-2019 Timothy Baxendale
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package tbax.baxshops.serialization;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxShop;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.notification.NoteSet;

import java.lang.reflect.Field;
import java.util.Collection;

public interface StateLoader
{
    @NotNull Collection<BaxShop> buildShops(@NotNull FileConfiguration state);
    @NotNull Collection<StoredPlayer> buildPlayers(@NotNull FileConfiguration state);
    @NotNull Collection<NoteSet> buildNotifications(@NotNull FileConfiguration state);

    @NotNull ShopPlugin getPlugin();

    default @NotNull BaxConfig loadConfig(@NotNull FileConfiguration config)
    {
        return new BaxConfig(getPlugin());
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
        }

        for (NoteSet noteSet : notes) {
            savedState.pending.put(noteSet.getRecipient(), noteSet.getNotifications());
        }

        savedState.config = loadConfig(getPlugin().getConfig());
        return savedState;
    }

    default double getVersion()
    {
        Field f = null;
        try {
            f = getClass().getField("VERSION");
        }
        catch (NoSuchFieldException e) {
            SerializationException.throwStateLoaderException(e);
        }
        double ver = 0d;
        try {
            f.get(null);
        }
        catch (IllegalAccessException | IllegalArgumentException e) {
            SerializationException.throwStateLoaderException(e);
        }
        return ver;
    }
}
