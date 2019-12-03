/*
 * Copyright (C) Timothy Baxendale
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
package org.tbax.baxshops.serialization;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.notification.Claimable;
import org.tbax.baxshops.notification.Notification;
import org.tbax.baxshops.notification.Request;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;

public interface StateLoader
{
    @NotNull Collection<BaxShop> buildShops(@NotNull FileConfiguration state);
    @NotNull Collection<StoredPlayer> buildPlayers(@NotNull FileConfiguration state);

    @NotNull ShopPlugin getPlugin();

    FileConfiguration readFile(@NotNull File stateLocation) throws IOException, InvalidConfigurationException;

    default State loadState(@NotNull File stateLocation) throws IOException, InvalidConfigurationException
    {
        FileConfiguration state = readFile(stateLocation);
        State savedState = new State(getPlugin());

        ShopPlugin.logInfo("Loading shop data...");
        Collection<BaxShop> shops = buildShops(state);
        sanitizeShopData(shops);
        ShopPlugin.logInfo("Loading player data...");
        Collection<StoredPlayer> players = buildPlayers(state);
        sanitizePlayerData(players);

        for (StoredPlayer player : players) {
            savedState.players.put(player);
            for (Notification n : player.getNotifications()) {
                n.setRecipient(player);
            }
        }

        for (BaxShop shop : shops) {
            savedState.shops.put(shop.getId(), shop);
        }

        return savedState;
    }

    default void sanitizeShopData(Collection<BaxShop> shops)
    {
        // do not sanitize by default
    }

    default void sanitizePlayerData(Collection<StoredPlayer> players)
    {
        boolean newErrNotes = false;
        for (StoredPlayer player : players) {
            if (StoredPlayer.DUMMY.equals(player)) { // don't save any dummy notes
                while (player.hasNotes()) {
                    Notification n = player.dequeueNote();
                    if (n instanceof Claimable || n instanceof Request) {
                        StoredPlayer.ERROR.queueNote(n);
                        newErrNotes = true;
                    }
                }
                if (newErrNotes) {
                    ShopPlugin.logWarning("There is one or more claim or request notification assigned to \"" + StoredPlayer.DUMMY_NAME + "\" who is not a real player. ");
                    ShopPlugin.logWarning("These requests cannot be honored and will be assigned to an error user. These can be fixed manually in the configuration file.");
                }
                break;
            }
        }
    }

    default double getVersion()
    {
        double ver = 0d;
        try {
            Field f = getClass().getField("VERSION");
            ver = (double)f.get(null);
        }
        catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | ClassCastException e) {
            SerializationException.throwStateLoaderException(e);
        }
        return ver;
    }

    default void addPlayer(State state, StoredPlayer storedPlayer)
    {
        state.players.put(storedPlayer);
    }

    default void addShop(State state, BaxShop shop)
    {
        state.shops.put(shop.getId(), shop);
    }
}
