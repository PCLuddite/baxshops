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
import tbax.baxshops.Format;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.serialization.states.State_00300;
import tbax.baxshops.serialization.states.State_00410;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class SavedState
{
    static final String YAML_FILE_PATH = "shops.yml";
    
    private static final double STATE_VERSION = State_00410.VERSION; // state file format version
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

    Configuration config;

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

    private void deleteLatestBackup(File backupFolder)
    {
        File[] backups = backupFolder.listFiles((f, name) -> name.endsWith(".yml"));
        int nBaks = getConfig().getBackups();

        if (backups == null || nBaks <= 0 || backups.length < nBaks) {
            return;
        }

        List<String> names = Arrays.stream(backups)
            .map(f -> f.getName().substring(0, f.getName().lastIndexOf('.')))
            .filter(n -> Format.parseFileDate(n) != null)
            .sorted(Comparator.comparing(Format::parseFileDate))
            .collect(Collectors.toList());

        while (names.size() >= nBaks) {
            File delete = new File(backupFolder, names.remove(names.size() - 1) + ".yml");
            if (!delete.delete()) {
                log.warning(String.format("Unable to delete old backup %s", delete.getName()));
            }
        }
    }

    /**
     * Attempts to back up the shops.yml save file.
     * @return a boolean indicating success
     */
    public boolean backup()
    {
        File stateLocation = new File(plugin.getDataFolder(), YAML_FILE_PATH);
        if (!stateLocation.exists()) {
            log.warning("Aborting backup: shops.yml not found");
            return false;
        }

        File backupFolder = new File(plugin.getDataFolder(), "backups");
        if (!backupFolder.exists() && !backupFolder.mkdirs()) {
            log.severe("Unable to create backups folder!");
            return false;
        }

        deleteLatestBackup(backupFolder);

        try {
            String backupName = Format.FILE_DATE_FORMAT.format(new Date()) + ".yml";
            File backup = new File(backupFolder, backupName);
            try (InputStream in = new FileInputStream(stateLocation)) {
                try (OutputStream out = new FileOutputStream(backup)) {
                    byte[] buf = new byte[1024];
                    int i;
                    while ((i = in.read(buf)) > 0) {
                        out.write(buf, 0, i);
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            log.severe("Backup failed!");
            return false;
        }
        return true;
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
        if (n == null) {
            n = new ArrayDeque<>();
            pending.put(player.getUniqueId(), n);
        }
        return n;
    }

    private void resaveConfig()
    {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.renameTo(new File(plugin.getDataFolder(), "config.bak"))) {
            plugin.getLogger().warning("Could not backup config. Configuration may be lost.");
        }
        plugin.getConfig().set("Backups", config.getBackups());
        plugin.getConfig().set("LogNotes", config.isLogNotes());
        plugin.getConfig().set("XPConvert", config.getXpConvert());
        plugin.getConfig().set("DeathTax.Enabled", config.isDeathTaxEnabled());
        plugin.getConfig().set("DeathTax.GoesTo", config.getDeathTaxGoesToId().toString());
        plugin.getConfig().set("DeathTax.Percentage", config.getDeathTaxPercentage());
        plugin.getConfig().set("DeathTax.Minimum", config.getDeathTaxMinimum());
        plugin.getConfig().set("DeathTax.Maximum", config.getDeathTaxMaximum());
        plugin.getConfig().set("StateVersion", STATE_VERSION);
        plugin.saveConfig();
    }

    /**
     * Saves all shops
     */
    public void writeToDisk()
    {
        if (!backup()) {
            log.warning("Failed to back up BaxShops");
        }

        if (loadedState != STATE_VERSION) {
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
        }
        catch (IOException e) {
            log.severe("Save failed");
            e.printStackTrace();
        }
    }

    public @NotNull StoredPlayer getOfflinePlayer(UUID uuid)
    {
        StoredPlayer player = players.get(uuid);
        assert player != null;
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

    public Configuration getConfig()
    {
        if (config == null)
            config = new Configuration();
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
        return players.values();
    }
}
