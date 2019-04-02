/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.Resources;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.serialization.states.State_30;
import tbax.baxshops.serialization.states.State_40;
import tbax.baxshops.serialization.states.State_41;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Logger;

public final class SavedState
{
    static final String YAML_FILE_PATH = "shops.yml";
    private static final String YAMLBAK_FILE_PATH = "backups/%d.yml";
    
    private static final double STATE_VERSION = State_41.VERSION; // state file format version
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
            ver = State_30.VERSION;
        }
        loadedState = ver;

        StateLoader loader;
        if (ver == State_41.VERSION) {
            loader = new State_41(plugin);
        }
        else if (ver == State_40.VERSION) {
            loader = new State_40(plugin);
        }
        else if (ver == State_30.VERSION) {
            loader = new State_30(plugin);
        }
        else {
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
        int b = plugin.getConfig().getInt("Backups", 15);
        if (backups != null && b > 0 && backups.length >= b) {
            File delete = null;
            long oldest = Long.MAX_VALUE;
            for (File f : backups) {
                String name = f.getName();
                int i = name.indexOf('.');
                try {
                    long compare = Long.parseLong(name.substring(0, i));
                    if (compare < oldest) {
                        oldest = compare;
                        delete = f;
                    }
                }
                catch (NumberFormatException ignored) {
                }
            }
            if (delete != null && !delete.delete()) {
                log.warning("Unable to delete oldest backup");
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

        long timestamp = new Date().getTime();
        File backupFolder = new File(plugin.getDataFolder(), "backups");
        if (!backupFolder.exists() && !backupFolder.mkdirs()) {
            log.severe("Unable to create backups folder!");
            return false;
        }

        deleteLatestBackup(backupFolder);

        try {
            File backup = new File(plugin.getDataFolder(), String.format(YAMLBAK_FILE_PATH, timestamp));
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
            log.severe("Backup failed!");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public void addShop(Player player, BaxShop shop)
    {
        shops.put(shop.getId(), shop);
    }
    
    public void addLocation(Player player, Location loc, BaxShop shop)
    {
        try {
            BaxShop otherShop = shops.getShopByLocation(loc);
            if (otherShop == null) {
                shops.addLocation(shop.getId(), loc);
            }
            else {
                throw new CommandErrorException(Resources.SHOP_EXISTS);
            }
        }
        catch (PrematureAbortException e) {
            player.sendMessage(e.getMessage());
        }
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
        log.info("Saving BaxShops...");

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
}
