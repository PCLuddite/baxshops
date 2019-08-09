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

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tbax.baxshops.BaxShop;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.serialization.states.State_00300;
import tbax.baxshops.serialization.states.State_00422;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class SavedState
{
    static final String YAML_FILE_PATH = "shops.yml";
    
    private static final double STATE_VERSION = State_00422.VERSION; // state file format version
    private static double loadedState;

    /**
     * A map of ids map to their shops
     */
    ShopMap shops = new ShopMap();
    /**
     * A map containing each player's notifications
     */
    Map<UUID, Deque<Notification>> pending = new HashMap<>();

    /**
     * A map containing each player's attributes for when they're offline
     */
    PlayerMap players = new PlayerMap();

    final ShopPlugin plugin;
    final Logger log;

    BaxConfig config;

    SavedState(@NotNull ShopPlugin plugin)
    {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        players.put(StoredPlayer.DUMMY);
        shops.put(BaxShop.DUMMY_UUID, BaxShop.DUMMY_SHOP);
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

    public static SavedState readFromDisk(@NotNull ShopPlugin plugin)
    {
        File stateLocation = new File(plugin.getDataFolder(), YAML_FILE_PATH);
        if (!stateLocation.exists()) {
            plugin.getLogger().info("YAML file did not exist");
            return new SavedState(plugin);
        }
        double ver;
        if (plugin.getConfig().contains("StateVersion")) {
            ver = plugin.getConfig().getDouble("StateVersion", STATE_VERSION);
        }
        else {
            ver = State_00300.VERSION; // version 3.0 was the last version not to be in config.yml
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
            plugin.getLogger().info("Converting state file version " + (new DecimalFormat("0.0")).format(ver));
        }

        return loader.loadState(YamlConfiguration.loadConfiguration(stateLocation));
    }

    /**
     * Attempts to back up the shops.yml save file.
     * @return a boolean indicating success
     */
    public boolean backup()
    {
        return BackupUtil.backup(this);
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

    /**
     * Gets a list of notifications for a player.
     *
     * @param player the player
     * @return the player's notifications
     */
    public @NotNull Deque<Notification> getNotifications(OfflinePlayer player)
    {
        Deque<Notification> n = pending.get(player.getUniqueId());
        if (n == null)
            pending.put(player.getUniqueId(), n = new ArrayDeque<>());
        return n;
    }

    private void resaveConfig()
    {
        if (!config.backup())
            plugin.getLogger().warning("Could not backup config. Configuration may be lost.");
        if (config.getStateVersion() != STATE_VERSION)
            config.getFileConfig().set("StateVersion", STATE_VERSION);
        config.save();
    }

    /**
     * Saves all shops
     */
    public void writeToDisk()
    {
        if (!backup()) {
            log.warning("Failed to back up BaxShops");
        }

        if (config.getStateVersion() != STATE_VERSION || config.saveDefaults()) {
            resaveConfig();
        }

        FileConfiguration state = new YamlConfiguration();
        state.set("shops", new ArrayList<>(shops.values()));

        ConfigurationSection notes = state.createSection("notes");
        for (Map.Entry<UUID, Deque<Notification>> entry : pending.entrySet()) {
            notes.set(entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }

        state.set("players", new ArrayList<>(players.values()));

        try {
            File dir = plugin.getDataFolder();
            if (!dir.exists() && !dir.mkdirs()) {
                log.severe("Unable to make data folder!");
            }
            else {
                state.save(new File(dir, YAML_FILE_PATH));
            }

            if (BackupUtil.hasStateChanged(this)) {
                BackupUtil.deleteOldestBackup(this);
            }
            else {
                BackupUtil.deleteLatestBackup(this);
            }
        }
        catch (IOException e) {
            log.severe("Save failed");
            e.printStackTrace();
        }
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

    public void joinPlayer(Player player)
    {
        StoredPlayer storedPlayer = players.convertLegacy(player);
        if (storedPlayer == null) {
            storedPlayer = new StoredPlayer(player);
        }
        else {
            Deque<Notification> notes = pending.remove(storedPlayer.getUniqueId());
            players.remove(storedPlayer.getUniqueId());
            if (notes != null)
                pending.put(storedPlayer.getUniqueId(), notes);
        }
        players.put(storedPlayer.getUniqueId(), storedPlayer);
    }

    public BaxConfig getConfig()
    {
        if (config == null)
            config = new BaxConfig(plugin);
        return config;
    }

    public void reload()
    {
        log.info("Reloading BaxShops...");
        writeToDisk();
        log.info("Clearing memory...");

        shops.clear();
        players.clear();
        pending.clear();

        log.info("Loading BaxShops...");
        SavedState savedState = readFromDisk(plugin);
        shops = savedState.shops;
        players = savedState.players;
        pending = savedState.pending;
        config = savedState.config;
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

    public BaxShop getShop(String shortId)
    {
        return shops.getShopByAbbreviatedId(shortId);
    }

    public File getFile()
    {
        return new File(plugin.getDataFolder(), SavedState.YAML_FILE_PATH);
    }

    public File getBackupFile()
    {
        return new File(plugin.getDataFolder(), "backups");
    }
}
