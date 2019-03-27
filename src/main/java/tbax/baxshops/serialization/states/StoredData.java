/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization.states;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.Resources;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.notification.NoteSet;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.serialization.PlayerMap;
import tbax.baxshops.serialization.StoredPlayer;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Logger;

public final class StoredData
{
    static final String YAML_FILE_PATH = "shops.yml";
    private static final String YAMLBAK_FILE_PATH = "backups/%d.yml";
    
    private static final double STATE_VERSION = 4.0; // state file format version

    /**
     * A map of locations to their shop ids, accessed by their location in the world
     */
    final Map<Location, UUID> locations = new HashMap<>();
    
    /**
     * A map of ids map to their shops
     */
    final Map<UUID, BaxShop> shops = new HashMap<>();
    /**
     * A map containing each player's notifications
     */
    final Map<UUID, Deque<Notification>> pending = new HashMap<>();

    /**
     * A map containing each player's attributes for when they're offline
     */
    final PlayerMap players = new PlayerMap();

    final ShopPlugin plugin;
    final Logger log;

    StoredData(ShopPlugin plugin)
    {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        players.put(StoredPlayer.DUMMY);
        shops.put(BaxShop.DUMMY_UUID, BaxShop.DUMMY_SHOP);
    }

    public BaxShop getShop(UUID uid)
    {
        return shops.get(uid);
    }
    
    public BaxShop getShop(Location loc)
    {
        UUID uid = locations.get(loc);
        if (uid == null) {
            return null;
        }
        return shops.get(uid);
    }
    
    public static StoredData load(ShopPlugin plugin)
    {
        File stateLocation = new File(plugin.getDataFolder(), YAML_FILE_PATH);
        if (!stateLocation.exists()) {
            plugin.getLogger().info("YAML file did not exist");
            return new StoredData(plugin);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(stateLocation);
        double ver = config.getDouble("version");

        StateLoader loader;
        if (ver == 4.0) {
            loader = new State_40(plugin);
        }
        else if (ver == 3.0) {
            loader = new State_30(plugin);
        }
        else {
            plugin.getLogger().warning("Unknown state file version. Starting from scratch...");
            return new StoredData(plugin);
        }

        if (ver != STATE_VERSION) {
            ShopPlugin.getInstance().getLogger().info("Converting state file version " + (new DecimalFormat("0.0")).format(ver));
        }

        return loader.load(config);
    }
    
    /**
     * Attempts to back up the shops.yml save file.
     * @return a boolean indicating success
     */
    public boolean backup()
    {
        File stateLocation = new File(plugin.getDataFolder(), YAML_FILE_PATH);
        if (stateLocation.exists()) {
            long timestamp = new Date().getTime();
            File backupFolder = new File(plugin.getDataFolder(), "backups");
            if (!backupFolder.exists()) {
                //noinspection ResultOfMethodCallIgnored
                backupFolder.mkdirs();
            }

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
                if (delete != null) {
                    //noinspection ResultOfMethodCallIgnored
                    delete.delete();
                }
            }

            try {
                File backup = new File(plugin.getDataFolder(), String.format(YAMLBAK_FILE_PATH, timestamp));
                OutputStream out;
                try (InputStream in = new FileInputStream(stateLocation)) {
                    out = new FileOutputStream(backup);
                    byte[] buf = new byte[1024];
                    int i;
                    while ((i = in.read(buf)) > 0) {
                        out.write(buf, 0, i);
                    }
                }
                out.close();
            } catch (IOException e) {
                log.warning("Backup failed");
                e.printStackTrace();
                return false;
            }
            return true;
        }
        log.warning("Aborting backup: shops.yml not found");
        return false;
    }
    
    public boolean addShop(Player player, BaxShop shop)
    {
        for(Location loc : shop.getLocations()) {
            if (!addLocation(player, loc, shop)) {
                return false;
            }
        }
        shops.put(shop.getId(), shop);
        return true;
    }
    
    public boolean addLocation(Player player, Location loc, BaxShop shop)
    {
        try {
            UUID otherUid = locations.get(loc);
            if (otherUid == null) {
                locations.put(loc, shop.getId());
            }
            else if (!otherUid.equals(shop.getId())) {
                throw new CommandErrorException("You can't create a new shop here! Another shop already exists on this block!");
            }
            return true;
        }
        catch (PrematureAbortException e) {
            player.sendMessage(e.getMessage());
            return false;
        }
    }
    
    public void removeShop(CommandSender sender, BaxShop shop) throws PrematureAbortException
    {
        for(Location loc : (Location[])shop.getLocations().toArray()) {
            removeLocation(sender, loc);
        }
        sender.sendMessage(String.format("%s's shop has been deleted.", Format.username(shop.getOwner().getName())));
    }
    
    public void removeLocation(CommandSender sender, Location loc) throws PrematureAbortException
    {
        UUID uid = locations.get(loc);
        if (uid != null) {
            BaxShop shop = shops.get(uid);
            try {
                Block b = loc.getBlock();
                Sign sign = (Sign) b.getState();
                sign.setLine(0, Resources.SIGN_CLOSED[0]);
                sign.setLine(1, Resources.SIGN_CLOSED[1]);
                sign.setLine(2, (shop.getOwner().equals(sender) ? "the owner" : "an admin") + ".");
                sign.setLine(3, "");
                sign.update();
            }
            catch(NullPointerException | ClassCastException e) {
                throw new CommandErrorException("Unable to change the sign text at " + Format.location(loc));
            }
            shop.removeLocation(loc);
            locations.remove(loc);
            if (shop.getLocations().isEmpty()) {
               shops.remove(shop.getId());
            }
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

    /**
     * Saves all shops
     */
    public void saveAll()
    {
        if (!backup()) {
            log.warning("Failed to back up BaxShops");
        }

        FileConfiguration state = new YamlConfiguration();
        state.set("version", STATE_VERSION);
        state.set("shops", new ArrayList<>(shops.values()));
        state.set("players", new ArrayList<>(players.values()));
        List<NoteSet> notes = new ArrayList<>();
        for(Map.Entry<UUID, Deque<Notification>> entry : pending.entrySet()) {
            notes.add(new NoteSet(entry.getKey(), entry.getValue()));
        }
        state.set("notes", notes);
        
        try {
            File dir = plugin.getDataFolder();
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }
            state.save(new File(dir, YAML_FILE_PATH));
        } catch (IOException e) {
            log.warning("Save failed");
            e.printStackTrace();
        }
    }

    public StoredPlayer getOfflinePlayer(UUID uuid)
    {
        StoredPlayer player = players.get(uuid);
        assert player != null;
        return player;
    }

    public StoredPlayer getOfflinePlayerSafe(UUID uuid)
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
}
