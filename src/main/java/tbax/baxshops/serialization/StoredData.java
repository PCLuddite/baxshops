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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.Resources;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.notification.NoteSet;
import tbax.baxshops.notification.Notification;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public final class StoredData
{
    private static final String YAML_FILE_PATH = "shops.yml";
    private static final String YAMLBAK_FILE_PATH = "backups/%d.yml";
    
    private static final double STATE_VERSION = 4.0; // state file format version

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
     * A map containing each player's attributes for when they're offline
     */
    private static PlayerMap players = new PlayerMap();

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
    
    /*
     * Loads all shops from shop.yml file
     */
    private static void loadState()
    {
        File stateLocation = new File(plugin.getDataFolder(), YAML_FILE_PATH);
        if (!stateLocation.exists()) {
            log.info("YAML file did not exist");
            return;
        }
        
        FileConfiguration state = YamlConfiguration.loadConfiguration(stateLocation);

        if (state.isList("shops")) {
            for (Object o : state.getList("shops")) {
                if (o instanceof BaxShop) {
                    BaxShop shop = (BaxShop) o;
                    for (Location loc : shop.getLocations()) {
                        locations.put(loc, shop.getId());
                    }
                    shops.put(shop.getId(), shop);
                }
                else {
                    log.warning("Could not load BaxShop of type " + o.getClass());
                }
            }
        }

        if (state.isList("notes")) {
            for (Object o : state.getList("notes")) {
                if (o instanceof NoteSet) {
                    NoteSet note = (NoteSet)o;
                    pending.put(note.getRecipient(), note.getNotifications());
                }
                else {
                    log.warning("Could not load NoteSet of type " + o.getClass());
                }
            }
        }

        if (state.isList("players")) {
            boolean hasWorld = false;
            for(Object o : state.getList("players")) {
                if (o instanceof StoredPlayer) {
                    StoredPlayer player = (StoredPlayer)o;
                    if (player.equals(StoredPlayer.DUMMY))
                        hasWorld = true;
                    players.put(player.getUniqueId(), player);
                }
                else {
                    log.warning("Could not load StoredPlayer of type " + o.getClass());
                }
            }
            if (!hasWorld)
                players.put(StoredPlayer.DUMMY_UUID, StoredPlayer.DUMMY);
        }
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
    public static Deque<Notification> getNotifications(OfflinePlayer player)
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
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }
            state.save(new File(dir, YAML_FILE_PATH));
        } catch (IOException e) {
            log.warning("Save failed");
            e.printStackTrace();
        }
    }

    public static StoredPlayer getOfflinePlayer(UUID uuid)
    {
        StoredPlayer player = players.get(uuid);
        assert player != null;
        return player;
    }

    public static StoredPlayer getOfflinePlayerSafe(UUID uuid)
    {
        StoredPlayer player = players.get(uuid);
        if (player == null) {
            player = new StoredPlayer(uuid.toString(), uuid);
            players.put(player);
        }
        return player;
    }

    public static List<StoredPlayer> getOfflinePlayer(String playerName)
    {
        return players.get(playerName);
    }

    public static void joinPlayer(Player player)
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
