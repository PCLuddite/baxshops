/* 
 * The MIT License
 *
 * Copyright © 2013-2015 Timothy Baxendale (pcluddite@hotmail.com) and 
 * Copyright © 2012 Nathan Dinsmore and Sam Lazarus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package tbax.baxshops;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import tbax.baxshops.executer.*;
import tbax.baxshops.notification.DeathNotification;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.serialization.StateFile;

public final class Main extends JavaPlugin implements Listener {
    
    /**
     * A single instance of Main for external access
     */
    public static Main instance;
    
    /**
     * A map containing each player's currently selected shop and other
     * selection data
     */
    public HashMap<Player, ShopSelection> selectedShops = new HashMap<>();
    /**
     * A map containing each player's current options shop and other
     * option data
     */
    public HashMap<Player, HashMap<String, Object>> tempSettings = new HashMap<>();
    
   
    /**
     * The file and text resources for the plugin
     */
    public StateFile state;
    
    /**
     * The Vault economy
     */
    public static Economy econ;
    
    public static Logger log;
    
    public Main() {
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        instance = this;
        log = this.getLogger();
        
        if (!enableVault()) {
            log.warning("BaxShops could not use this server's economy! Make sure Vault is installed!");
            this.getPluginLoader().disablePlugin(this);
            return;
        }
        
        state = new StateFile(this);
        state.load();
        
        saveDefaultConfig();
        
        // run an initial save 5 minutes after starting, then a recurring save
        // every 30 minutes after the first save
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                state.saveAll();
            }
        }, 6000L, 36000L);
        log.info("BaxShops has loaded successfully!");
    }
    
    @Override
    public void onDisable() {
        state.saveAll();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                
        ShopCmd cmd = new ShopCmd(this, sender, command, args);
        
        if (cmd.getName().equalsIgnoreCase("buy") || cmd.getName().equalsIgnoreCase("sell") ||
            cmd.getName().equalsIgnoreCase("restock") || cmd.getName().equalsIgnoreCase("restockall")) {
            cmd.insertAction(cmd.getName());
            cmd.setName("shop");
        }
        
        if (cmd.getNumArgs() == 0) {
           Help.showHelp(sender);
           return true;
        }
        
        if (MainExecuter.execute(cmd)){
            return true;
        }     
        
        // Sender is not a player
        if (cmd.getPlayer() == null && cmd.getNumArgs() > 0) {
            if (cmd.getArg(0).equalsIgnoreCase("removeallnotifications")) {
                state.pending.clear();
            }
            else {
                sendError(sender, "/shop commands can only be used by a player");
            }
            return true;
        }
        /*
        exec = new BlacklistExecuter(sender, command, label, args);
        if (exec.execute(action, this)) {
            return true;
        } */
        if (ShopExecuter.execute(cmd)) {
            return true;
        }
        if (FlagCmdExecuter.execute(cmd)) {
            return true;
        }
        if (RefCmdExecuter.execute(cmd)) {
            return true;
        }
        return NotifyExecuter.execute(cmd);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Location loc = block.getLocation();
        loc.setY(loc.getY() + 1);
        if (state.getShop(loc) != null) {
            sendError(event.getPlayer(), "You cannot remove this block because there is a shop above it!");
            event.setCancelled(true); 
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block b = event.getClickedBlock();
        if (b == null) {
            return;
        }
        
        BaxShop shop = state.getShop(b.getLocation());
        if (shop == null) {
            return;
        }
        Player pl = event.getPlayer();
        
        boolean isOwner = shop.owner.equals(pl.getName());
        
        /*if (!pl.hasPermission("shop.admin") && !isOwner) {
            if ((boolean)shop.getOption("whitelist_enabled") &&
                !((ArrayList<String>)shop.getOption("whitelist")).contains(pl.getName())) {
                sendError(pl, "You need to be whitelisted at this shop to view its inventory.");
                return;
            }
            if ((boolean)shop.getOption("blacklist_enabled") &&
                ((ArrayList<String>)shop.getOption("blacklist")).contains(pl.getName())) {
                sendError(pl, "You have been banned from this shop and cannot view its inventory.");
                return;
            }
        }*/
        
        ShopSelection selection = selectedShops.get(pl);
        if (selection == null) {
            selection = new ShopSelection();
            selectedShops.put(pl, selection);
        }
        selection.location = b.getLocation();
        
        //res.log.log(Level.INFO, "{0} selected {1}''s shop at ({2}, {3}, {4})", new Object[]{pl.getName(), shop.owner, selection.location.getBlockX(), selection.location.getBlockY(), selection.location.getBlockX()});
        
        if (selection.shop == shop) {
            int pages = shop.getPages();
            if (pages == 0) {
                selection.page = 0;
            }
            else {
                int delta = event.getAction() == Action.LEFT_CLICK_BLOCK ? -1 : 1;
                selection.page = (((selection.page + delta) % pages) + pages) % pages;
            }
            pl.sendMessage("");
            pl.sendMessage("");
        }
        else {
            selection.isOwner = isOwner;
            selection.shop = shop;
            selection.page = 0;
            StringBuilder intro = new StringBuilder(ChatColor.WHITE.toString());
            intro.append("Welcome to ");
            if (isOwner) {
                intro.append(Format.username("your"));
            }
            else {
                intro.append(Format.username(shop.owner)).append("'s");
            }
            intro.append(" shop\n");
            intro.append(ChatColor.GRAY.toString());
            intro.append("For help with shops, type /shop help.");
            pl.sendMessage(intro.toString());
        }

        selection.showListing(pl);

        event.setCancelled(true);
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            b.getState().update();
        }
    }
	
    @EventHandler(priority = EventPriority.HIGH)
    public void onExplosion(EntityExplodeEvent event) {
        for (Block b : event.blockList()) {
            Location loc = b.getLocation();
            if (state.getShop(loc) != null) {
                event.setCancelled(true);
                return;
            }
            loc.setY(loc.getY() + 1);
            if (state.getShop(loc) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        ArrayDeque<Notification> p = state.getNotifications(event.getPlayer());
        if (!p.isEmpty()) {
            event.getPlayer().sendMessage(ChatColor.WHITE + "You have new notifications. Use " + Format.command("/shop notifications") + ChatColor.WHITE + " to view them");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player pl = event.getPlayer();
        ShopSelection s = selectedShops.get(pl);
        if (s != null) {
            Location shopLoc = s.location;
            Location pLoc = event.getTo();
            if (shopLoc.getWorld() != pl.getWorld() || shopLoc.distanceSquared(pLoc) > Resources.SHOP_RANGE) {
                if (s.isOwner) {
                    pl.sendMessage("[Left " + Format.username("your") + " shop]");
                }
                else {
                    pl.sendMessage("[Left " + Format.username(s.shop.owner) + "'s shop]");
                }
                selectedShops.remove(event.getPlayer());
                clearTempOpts(pl);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        String name = getConfig().getString("DeathTax.GoesTo", null);
        if (name != null) {
            Player pl = event.getEntity();
            if (econ.has(pl.getName(), 100.00) && isStupidDeath(event.getDeathMessage())) {
                double death_tax = econ.getBalance(pl.getName()) * getConfig().getDouble("DeathTax.Percentage", 0.04);
                econ.withdrawPlayer(pl.getName(), death_tax);
                econ.depositPlayer(name, death_tax);
                state.sendNotification(pl, new DeathNotification(pl.getName(), death_tax));
            }
        }
    }
    
    private static boolean isStupidDeath(String death) {
        return (death.contains("fell") && !death.contains("world")) ||
               death.endsWith("drowned") ||
               death.endsWith("lava") ||
               death.contains("pricked") ||
               death.contains("suffocated");
    }
    
    /**
     * Sets up Vault.
     *
     * @return true on success, false otherwise
     */
    private boolean enableVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        econ = rsp.getProvider();
        return econ != null;
    }
    
    /**
     * Informs a player of an error.
     *
     * @param sender the player
     * @param message the error message
     */
    public static void sendError(CommandSender sender, String message)
    {
        if (sender != null) {
            sender.sendMessage(ChatColor.DARK_RED + message);
        }
    }
    
    /**
     * Checks whether an item stack will fit in a player's inventory.
     *
     * @param pl the player
     * @param item the item
     * @return whether the item will fit
     */
    public static boolean inventoryFitsItem(Player pl, ItemStack item)
    {
        int quantity = item.getAmount(),
            damage   = item.getDurability(),
            max      = item.getMaxStackSize();
        Material id = item.getType();
        Inventory inv = pl.getInventory();
        ItemStack[] contents = inv.getContents();
        ItemStack s;
        if (max == -1) {
            max = inv.getMaxStackSize();
        }
        for (int i = 0; i < contents.length; ++i) {
            if ((s = contents[i]) == null || s.getType() == Material.AIR) {
                quantity -= max;
                if (quantity <= 0) {
                    return true;
                }
                continue;
            }
            if (s.getType() == id && s.getDurability() == damage) {
                quantity -= max - s.getAmount();
                if (quantity <= 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Gives as much of an item as possible to a player
     *
     * @param pl the player receiving the item
     * @param item the item to give
     * @return the number of items that could not be added
     */
    public static int giveItem(Player pl, ItemStack item)
    {
        int left = item.getAmount(), // how many items are left to add
            max      = item.getMaxStackSize();
        Inventory inv = pl.getInventory();
        if (max == -1) {
            max = inv.getMaxStackSize();
        }
        for (int i = 0; i < inv.getSize(); ++i) {
            ItemStack curr = inv.getItem(i);
            if (curr == null || curr.getType() == Material.AIR) {
                left -= max;
                if (left > 0) {
                    pl.getInventory().addItem(new ItemStack(item.getType(), max, item.getDurability()));
                }
                else {
                    pl.getInventory().addItem(new ItemStack(item.getType(), left + max, item.getDurability()));
                    return 0; // everything could fit
                }
            }
            else if (curr.getType() == item.getType() && curr.getDurability() == item.getDurability()) {
                left -= max - curr.getAmount();
                if (left > 0) {
                    curr.setAmount(max);
                }
                else {
                    curr.setAmount(left + (max - curr.getAmount()));
                    return 0; // everything could fit
                }
            }
        }
        return left;
    }
    
    public static boolean tryGiveItem(Player player, ItemStack item)
    {
        if (inventoryFitsItem(player, item)){
            giveItem(player, item);
            return true;
        }
        else {
            return false;
        }
    }
    
    public void clearTempOpts(Player pl)
    {
        HashMap<String, Object> opts = tempSettings.get(pl);
        if (opts != null) {
            opts.clear();
        }
    }
    
    public Object getTempOpt(Player pl, String option)
    {
        HashMap<String, Object> opts = tempSettings.get(pl);
        if (opts == null) {
            return null;
        }
        else {
            return opts.get(option);
        }
    }
    
    public void setTempOpt(Player pl, String option, Object obj)
    {
        HashMap<String, Object> opts = tempSettings.get(pl);
        if (opts == null) {
            opts = new HashMap<>();
            tempSettings.put(pl, opts);
        }
        opts.put(option, obj);
    }
    
    public boolean getTempOptBool(Player pl, String option)
    {
        if (getTempOpt(pl, option) == null) {
            return false;
        }
        else {
            return (boolean)getTempOpt(pl, option);
        }
    }
    
    public void removeSelection(Player pl)
    {
        if (selectedShops.get(pl) != null) {
            selectedShops.remove(pl);
            clearTempOpts(pl);
        }
    }
    
    /**
     * Gets around the imprecision of the double type
     * @param value the value to round
     * @return the rounded value
     */
    public static double roundTwoPlaces(double value)
    {
        return Math.round(value*100.0d)/100.0d;
    }
    
    public void sendInfo(Player pl, String message)
    {
        if (pl != null) {
            if (getConfig().getBoolean("LogNotes", false)) {
                pl.sendMessage(message);
                logPlayerMessage(pl, message);
            }
        }
    }
    
    public static void logPlayerMessage(Player pl, String message)
    { 
        StringBuilder sb = new StringBuilder();
        if (pl != null) {
            sb.append((char)27).append("[0;35m");
            sb.append("[");
            sb.append(pl.getName()).append("] ");
            sb.append((char)27);
            sb.append("[0m");
        }
        sb.append(toAnsiColor(message));
        log.info(sb.toString());
    }
    
    public static String toAnsiColor(String message) // obnoxious method to convert minecraft message colors to ansi colors
    {
        StringBuilder sb = new StringBuilder();
        boolean has_ansi = false;
        for(int index = 0; index < message.length(); ++index) {
            char c = message.charAt(index);
            if (c == '§' && ++index < message.length()) {
                c = Character.toLowerCase(message.charAt(index));
                sb.append((char)27);
                sb.append("[0;");
                switch(c) {
                    case '0': sb.append("30"); break;
                    case '1': sb.append("34"); break;
                    case '2': sb.append("32"); break;
                    case '3': sb.append("36"); break;
                    case '4': sb.append("31"); break;
                    case '5': sb.append("35"); break;
                    case '6': sb.append("33"); break;
                    case '7': sb.append("37"); break;
                    case '8': sb.append("37"); break;
                    case '9': sb.append("36"); break;
                    case 'a': sb.append("32"); break;
                    case 'b': sb.append("36"); break;
                    case 'c': sb.append("31"); break;
                    case 'd': sb.append("35"); break;
                    case 'e': sb.append("33"); break;
                    case 'f': sb.append("37"); break;
                    default:
                        sb.append("37"); break;
                }
                sb.append("m");
                if (!has_ansi) {
                    has_ansi = true;
                }
            }
            else {
                sb.append(c);
            }
        }
        if (has_ansi) {
            sb.append((char)27);
            sb.append("[0m"); // reset the color
        }
        return sb.toString();
    }
}
