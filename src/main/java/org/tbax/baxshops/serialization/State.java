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

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.bukkit.serialization.PlayerMap;
import org.tbax.bukkit.serialization.StoredPlayer;
import org.tbax.baxshops.serialization.states.*;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class State
{
    static final double STATE_VERSION = StateLoader_00481.VERSION; // state file format version

    private static double loadedState;
    private final StateFile stateFile;

    /**
     * A map of ids map to their shops
     */
    private ShopMap shops = new ShopMap();

    /**
     * A map containing each player's attributes for when they're offline
     */
    private PlayerMap players;

    private final ShopPlugin plugin;

    public State(@NotNull ShopPlugin plugin)
    {
        stateFile = new StateFile(plugin);
        this.plugin = plugin;
    }

    public static double getLoadedState()
    {
        return loadedState;
    }

    public @Nullable BaxShop getShop(UUID uid)
    {
        return shops.get(uid);
    }

    public @Nullable BaxShop getShop(Location loc)
    {
        return shops.getShopByLocation(loc);
    }

    public static State readFromDisk(@NotNull ShopPlugin plugin) throws IOException, InvalidConfigurationException
    {
        File stateLocation = ShopPlugin.getStateFile().getFile();
        loadedState = ShopPlugin.getStateFile().findVersion();
        if (loadedState == 0d) {
            if (stateLocation.exists()) {
                ShopPlugin.logWarning("Unable to read file format version! The save may be corrupt.");
                return null;
            }
            else if (StateLoader_00050.getState2File(plugin).exists()) {
                if (StateLoader_00050.getSerializedClass(plugin) == qs.shops.serialization.State.class) {
                    loadedState = StateLoader_00000.VERSION;
                    ShopPlugin.logInfo("Beginning conversion from nathan/shops");
                    return new StateLoader_00000(plugin).loadState(StateLoader_00000.getNathanFile(plugin));
                }
                else {
                    loadedState = StateLoader_00050.VERSION;
                    ShopPlugin.logInfo("Beginning conversion from tbax.shops.serialization.State2");
                    return new StateLoader_00050(plugin).loadState(StateLoader_00050.getState2File(plugin));
                }
            }
            else {
                loadedState = STATE_VERSION;
                ShopPlugin.logInfo("No save was found. Starting from scratch.");
                return new State(plugin);
            }
        }

        StateLoader loader;
        try {
            loader = UpgradeableSerialization.getStateLoader(plugin, loadedState);
        }
        catch (ReflectiveOperationException e) {
            ShopPlugin.logWarning("Unknown state file version. Starting from scratch...");
            return new State(plugin);
        }

        if (loadedState != STATE_VERSION) {
            ShopPlugin.logInfo("Converting state file version " + (new DecimalFormat("0.0#")).format(loadedState));
        }
        return loader.loadState(stateLocation);
    }

    public void addShop(BaxShop shop)
    {
        shops.put(shop.getId(), shop);
    }

    public boolean addLocation(BaxShop shop, Location loc)
    {
        BaxShop otherShop = shops.getShopByLocation(loc);
        if (otherShop == null) {
            shops.addLocation(shop.getId(), loc);
            return true;
        }
        return false;
    }

    public @NotNull StoredPlayer getOfflinePlayer(UUID uuid)
    {
        StoredPlayer player = players.get(uuid);
        if (player == null)
            return StoredPlayer.ERROR;
        return player;
    }

    public @NotNull StoredPlayer getOfflinePlayerSafe(UUID uuid)
    {
        StoredPlayer player = players.get(uuid);
        if (player == null) {
            player = new StoredPlayer(uuid.toString(), uuid);
            players.put(player);
        }
        return player;
    }

    public List<StoredPlayer> getOfflinePlayerSafe(String playerName)
    {
        return players.getOrCreate(playerName);
    }

    public List<StoredPlayer> getOfflinePlayer(String playerName)
    {
        return players.get(playerName);
    }

    /**
     * Makes sure an online player is in the player map and update the last seen name and ID
     * @param player the player
     */
    public StoredPlayer joinPlayer(Player player)
    {
        StoredPlayer storedPlayer = players.get(player.getUniqueId());
        if (storedPlayer == null && (storedPlayer = players.get(players.convertLegacy(player))) == null) {
            storedPlayer = new StoredPlayer(player);
            players.put(storedPlayer);
        }
        return storedPlayer;
    }

    public void reload() throws IOException, InvalidConfigurationException
    {
        ShopPlugin.logInfo("Reloading BaxShops...");
        stateFile.writeToDisk(this);
        ShopPlugin.logInfo("Clearing memory...");

        shops.clear();
        players.clear();

        ShopPlugin.logInfo("Reloading BaxShops...");
        State state = readFromDisk(plugin);
        shops = state.shops;
        players = state.players;
        ShopPlugin.logInfo("BaxShops has finished reloading");
    }

    public void removeLocation(UUID shopId, Location loc)
    {
        shops.removeLocation(shopId, loc);
    }

    public void removeShop(UUID shopId)
    {
        shops.remove(shopId);
    }

    public Collection<StoredPlayer> getRegisteredPlayers()
    {
        return players.values().stream()
                .filter(n -> !StoredPlayer.ERROR.equals(n))
                .collect(Collectors.toList());
    }

    @Deprecated
    public BaxShop getShopByShortId(String shortId)
    {
        return shops.getShopByShortId(shortId);
    }

    public BaxShop getShopByShortId2(String shortId2)
    {
        return shops.getShopByShortId2(shortId2);
    }

    public void setPlayers(Collection<StoredPlayer> players)
    {
        this.players = new PlayerMap(players);
    }

    public void setShops(Collection<BaxShop> baxShops)
    {
        for (BaxShop shop : baxShops) {
            shops.put(shop.getId(), shop);
        }
    }

    public Collection<BaxShop> getShops()
    {
        return shops.values();
    }

    public Collection<StoredPlayer> getPlayers()
    {
        return players.values();
    }
}
