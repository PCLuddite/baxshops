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
package org.tbax.baxshops.serialization;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.serialization.states.State_00000;
import org.tbax.baxshops.serialization.states.State_00300;
import org.tbax.baxshops.serialization.states.State_00470;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class SavedState
{
    static final double STATE_VERSION = State_00470.VERSION; // state file format version

    private static double loadedState;
    private final StateFile stateFile;

    /**
     * A map of ids map to their shops
     */
    ShopMap shops = new ShopMap();

    /**
     * A map containing each player's attributes for when they're offline
     */
    PlayerMap players = new PlayerMap();

    final ShopPlugin plugin;
    final Logger log;

    public SavedState(@NotNull ShopPlugin plugin)
    {
        stateFile = new StateFile(plugin);
        this.plugin = plugin;
        this.log = plugin.getLogger();
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

    public static SavedState readFromDisk(@NotNull ShopPlugin plugin) throws IOException
    {
        File stateLocation = ShopPlugin.getStateFile().getFile();
        if (!stateLocation.exists()) {
            if (State_00000.getNathanFile(plugin).exists()) {
                plugin.getLogger().info("Beginning conversion from nathan/shops");
                return new State_00000(plugin).loadState(new YamlConfiguration());
            }
            else {
                plugin.getLogger().info("YAML file did not exist. Starting fresh.");
                return new SavedState(plugin);
            }
        }
        double ver = StateFile.readVersion(stateLocation);
        if (ver == 0d) {
            if (plugin.getConfig().contains("StateVersion")) {
                ver = plugin.getConfig().getDouble("StateVersion", STATE_VERSION);
            }
            else {
                ver = State_00300.VERSION; // version 3.0 was the last version not to be in config.yml
            }
        }

        loadedState = ver;

        StateLoader loader;
        try {
            loader = UpgradeableSerialization.getStateLoader(plugin, ver);
        }
        catch (ReflectiveOperationException e) {
            plugin.getLogger().warning("Unknown state file version. Starting from scratch...");
            return new SavedState(plugin);
        }

        if (ver != STATE_VERSION) {
            plugin.getLogger().info("Converting state file version " + (new DecimalFormat("0.0#")).format(ver));
        }

        if (ver >= State_00470.VERSION) {
            try (BufferedReader reader = new BufferedReader(new FileReader(stateLocation))) {
                reader.readLine();
                return loader.loadState(YamlConfiguration.loadConfiguration(reader));
            }
        }
        else {
            return loader.loadState(YamlConfiguration.loadConfiguration(stateLocation));
        }
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

    public void reload() throws IOException
    {
        log.info("Reloading BaxShops...");
        stateFile.writeToDisk(this);
        log.info("Clearing memory...");

        shops.clear();
        players.clear();

        log.info("Reloading BaxShops...");
        SavedState savedState = readFromDisk(plugin);
        shops = savedState.shops;
        players = savedState.players;
        log.info("BaxShops has finished reloading");
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
}
