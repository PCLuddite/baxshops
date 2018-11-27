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
import tbax.baxshops.*;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.notification.Claimable;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.notification.Request;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public final class SavedData
{
    public static final String YAML_FILE_PATH = "shops.yml";
    public static final String YAMLBAK_FILE_PATH = "backups/%d.yml";
    
    public static final double STATE_VERSION = 4.0; // state file format version

    /**
     * A map containing each player's currently selected shop and other
     * selection data
     */
    private static HashMap<Player, ShopSelection> selectedShops = new HashMap<>();
    
    /**
     * A map of locations to their shop ids, accessed by their location in the world
     */
    private static HashMap<Location, UUID> locations = new HashMap<>();
    
    /**
     * A map of ids map to their shops
     */
    private static HashMap<UUID, BaxShop> shops = new HashMap<>();
    /**
     * A map containing each player's notifications
     */
    private static HashMap<UUID, ArrayDeque<Notification>> pending = new HashMap<>();

    private static Main plugin;
    private static Logger log;

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
    
    public static void load(Main main)
    {
        plugin = main;
        log = main.getLogger();

        ItemNames.loadDamageable(main);
        ItemNames.loadEnchants(main);
        
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
        
        ArrayList<BaxShop> shoplist = (ArrayList)state.getList("shops");
        for(BaxShop shop : shoplist) {
            addShop(null, shop);
        }

        ConfigurationSection yNotes = state.getConfigurationSection("notes");
        for(Map.Entry<String, Object> player : yNotes.getValues(false).entrySet()) {
            ArrayDeque<Notification> playerNotes = new ArrayDeque<>();
            UUID uuid = UUID.fromString(player.getKey());

            List yPlayerNotes = (List)player.getValue();
            for(Object yNote : yPlayerNotes) {
                playerNotes.add((Notification)yNote);
            }
            pending.put(UUID.fromString(player.getKey()), playerNotes);
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
    public static ArrayDeque<Notification> getNotifications(OfflinePlayer player)
    {
        ArrayDeque<Notification> n = pending.get(player);
        if (n == null) {
            n = new ArrayDeque<>();
            pending.put(player.getUniqueId(), n);
        }
        return n;
    }
    
    /**
     * Shows a player his/her most recent notification. Also shows the
     * notification count.
     *
     * @param player the player
     */
    public static void showNotification(Player player)
    {
        showNotification(player, true);
    }

    /**
     * Shows a player his/her most recent notification.
     *
     * @param player the player
     * @param showCount whether the notification count should be shown as well
     */
    public static void showNotification(Player player, boolean showCount)
    {
        ArrayDeque<Notification> notifications = getNotifications(player.getPlayer());
        if (notifications.isEmpty()) {
            if (showCount) {
                player.sendMessage("You have no notifications.");
            }
            return;
        }
        if (showCount) {
            int size = notifications.size();
            player.sendMessage(String.format("You have %s %s.", Format.number(size), size == 1 ? "notification" : "notifications"));
        }

        Notification n = notifications.getFirst();
        player.sendMessage(n.getMessage(player.getPlayer()));
        if (n instanceof Request) {
            player.sendMessage(String.format("Use %s or %s to manage this request.", Format.command("/shop accept"), Format.command("/shop reject")));
        } 
        else if (n instanceof Claimable) {
            player.sendMessage(String.format("Use %s to claim and remove this notification.", Format.command("/shop claim")));
        } 
        else {
            notifications.removeFirst();
        }
    }

    /**
     * Sends a notification to a player.
     *
     * @param player the player
     * @param n the notification
     */
    public static void sendNotification(OfflinePlayer player, Notification n)
    {
        sendNotification(player, n, plugin.getConfig().getBoolean("LogNotes"));
    }

    @Deprecated
    public static void sendNotification(String playerName, Notification n)
    {
        sendNotification(plugin.getServer().getOfflinePlayer(playerName), n);
    }

    /**
     * Sends a notification to a player.
     *
     * @param player the player
     * @param n the notification
     * @param logNote should show it in the log
     */
    public static void sendNotification(OfflinePlayer player, Notification n, boolean logNote)
    {
        ArrayDeque<Notification> ns = getNotifications(player);
        if (logNote) {
            log.info(Format.toAnsiColor(n.getMessage(null)));
        }
        ns.add(n);
        if (player != null && player.isOnline()) {
            showNotification(player.getPlayer(), false);
        }
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
        int invalid = 0;
        HashMap<String, List<String>> notes = new HashMap<>();
        for(Map.Entry<UUID, ArrayDeque<Notification>> entry : pending.entrySet()) {
            ArrayList ylist = new ArrayList();
            ylist.addAll(entry.getValue());
            notes.put(entry.getKey().toString(), ylist);
        }
        
        state.createSection("notes", notes);
        
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

    public static ShopSelection getSelection(Player player)
    {
        ShopSelection selected = selectedShops.get(player);
        if (selected == null) {
            selected = new ShopSelection();
            selectedShops.put(player, selected);
        }
        return selected;
    }

    public static void clearSelection(Player player)
    {
        if (selectedShops.containsKey(player)) {
            selectedShops.remove(player);
        }
    }
}
