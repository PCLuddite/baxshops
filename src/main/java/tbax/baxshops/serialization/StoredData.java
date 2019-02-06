/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.serialization;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import tbax.baxshops.*;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.notification.NoteSet;
import tbax.baxshops.notification.Notification;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public final class StoredData
{
    public static final String YAML_FILE_PATH = "shops.yml";
    public static final String YAMLBAK_FILE_PATH = "backups/%d.yml";
    
    public static final double STATE_VERSION = 4.0; // state file format version

    /**
     * A map of locations to their shop ids, accessed by their location in the world
     */
    private static Map<Location, UUID> locations = new HashMap<>();
    
    /**
     * A map of ids map to their shops
     */
    private static Map<UUID, BaxShop> shops = new HashMap<>();
    /**
     * A map containing each player's notifications
     */
    private static Map<UUID, Deque<Notification>> pending = new HashMap<>();

    /**
     * A map containing each player's display names for when they're offline
     */
    private static Map<UUID, StoredPlayer> players = new HashMap<>();

    private static ShopPlugin plugin;
    private static Logger log;

    private StoredData()
    {
    }

    public static BaxShop getShop(UUID uid)
    {
        return shops.get(uid);
    }
    
    public static BaxShop getShop(Location loc)
    {
        UUID uid = locations.get(loc);
        if (uid == null) {
            return null;
        }
        return shops.get(uid);
    }
    
    public static void load(ShopPlugin plugin)
    {
        StoredData.plugin = plugin;
        log = plugin.getLogger();

        ItemNames.loadDamageable(plugin);
        ItemNames.loadEnchants(plugin);
        
        loadState();
        
        if (shops == null) {
            shops = new HashMap<>();
            log.warning("BaxShops could not load saved data. This is expected upon installation. If this is not BaxShop's first run, try restarting with one of the backups.");
        }
    }
    
    /**
     * Attempts to back up the shops.yml save file.
     * @return a boolean indicating success
     */
    public static boolean backup()
    {
        File stateLocation = new File(plugin.getDataFolder(), YAML_FILE_PATH);
        if (stateLocation.exists()) {
            long timestamp = new Date().getTime();
            File backupFolder = new File(plugin.getDataFolder(), "backups");
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }

            File[] backups = backupFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File f, String name) {
                    return name.endsWith(".yml");
                }
            });
            int b = plugin.getConfig().getInt("Backups", 15);
            if (b > 0 && backups.length >= b) {
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
                    } catch (NumberFormatException e) {
                    }
                }
                if (delete != null) {
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
            } catch (FileNotFoundException e) {
                log.warning("Backup failed");
                e.printStackTrace();
                return false;
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
    
    /*
     * Loads all shops from shop.yml file
     */
    public static boolean loadState()
    {
        File stateLocation = new File(plugin.getDataFolder(), YAML_FILE_PATH);
        if (!stateLocation.exists()) {
            log.info("YAML file did not exist");
            return false;
        }
        
        FileConfiguration state = YamlConfiguration.loadConfiguration(stateLocation);
        
        List<BaxShop> shoplist = (List)state.getList("shops");
        for(BaxShop shop : shoplist) {
            for (Location loc : shop.getLocations()) {
                locations.put(loc, shop.getId());
            }
            shops.put(shop.getId(), shop);
        }

        List<NoteSet> notes = (List)state.getList("notes");
        if (notes != null) {
            for (NoteSet note : notes) {
                pending.put(note.getRecipient(), note.getNotifications());
            }
        }

        List<StoredPlayer> playerList = (List)state.getList("players");
        if (playerList != null) {
            for (StoredPlayer player : playerList) {
                players.put(player.getUniqueId(), player);
            }
        }
        return true;
    }
    
    public static boolean addShop(Player player, BaxShop shop)
    {
        for(Location loc : shop.getLocations()) {
            if (!addLocation(player, loc, shop)) {
                return false;
            }
        }
        shops.put(shop.getId(), shop);
        return true;
    }
    
    public static boolean addLocation(Player player, Location loc, BaxShop shop)
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
    
    public static void removeShop(CommandSender sender, BaxShop shop) throws PrematureAbortException
    {
        for(Location loc : (Location[])shop.getLocations().toArray()) {
            removeLocation(sender, loc);
        }
        sender.sendMessage(String.format("%s's shop has been deleted.", Format.username(shop.getOwner().getName())));
    }
    
    public static void removeLocation(CommandSender sender, Location loc) throws PrematureAbortException
    {
        UUID uid = locations.get(loc);
        if (uid != null) {
            BaxShop shop = shops.get(uid);
            try {
                Block b = loc.getBlock();
                Sign sign = (Sign) b.getState();
                sign.setLine(0, Resources.SIGN_CLOSED[0]);
                sign.setLine(1, Resources.SIGN_CLOSED[1]);
                sign.setLine(2, (sender.getName().equals(shop.getOwner()) ? "the owner" : "an admin") + ".");
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
    public static Deque<Notification> getNotifications(OfflinePlayer player)
    {
        Deque<Notification> n = pending.get(player);
        if (n == null) {
            n = new ArrayDeque<>();
            pending.put(player.getUniqueId(), n);
        }
        return n;
    }

    /**
     * Saves all shops
     */
    public static void saveAll()
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
                dir.mkdirs();
            }
            state.save(new File(dir, YAML_FILE_PATH));
        } catch (IOException e) {
            log.warning("Save failed");
            e.printStackTrace();
        }
    }

    public static OfflinePlayer getOfflinePlayer(UUID uuid)
    {
        StoredPlayer player = players.get(uuid);
        if (player == null) {
            return Bukkit.getOfflinePlayer(uuid);
        }
        return player;
    }

    @Deprecated
    public static OfflinePlayer getOfflinePlayer(String playerName) throws PrematureAbortException
    {
        for (StoredPlayer player : players.values()) {
            if (player.getName().equals(playerName)) {
                return player;
            }
        }
        throw new CommandErrorException("This player has not been set up yet. The player must log in at least once.");
    }

    public static StoredPlayer joinPlayer(Player player)
    {
        return players.put(player.getUniqueId(), new StoredPlayer(player));
    }

}
