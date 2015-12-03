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
import org.bukkit.configuration.serialization.ConfigurationSerialization;
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
import tbax.baxshops.notification.*;
import tbax.baxshops.serialization.StateFile;

public final class Main extends JavaPlugin implements Listener
{    
    /**
     * A map containing each player's currently selected shop and other
     * selection data
     */
    public HashMap<Player, ShopSelection> selectedShops = new HashMap<>();

    /**
     * The file and text resources for the plugin
     */
    private static StateFile state;
    
    /**
     * The Vault economy
     */
    public static Economy econ;
    
    public static Logger log;
    
    public Main()
    {
    }

    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
        log = this.getLogger();
        
        if (!enableVault()) {
            log.warning("BaxShops could not use this server's economy! Make sure Vault is installed!");
            this.getPluginLoader().disablePlugin(this);
            return;
        }
        
        loadConfigurationSerializable();
        
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
    
    public void loadConfigurationSerializable()
    {
        ConfigurationSerialization.registerClass(BaxEntry.class);
        ConfigurationSerialization.registerClass(BaxShop.class);
        ConfigurationSerialization.registerClass(BuyClaim.class);
        ConfigurationSerialization.registerClass(BuyNotification.class);
        ConfigurationSerialization.registerClass(BuyRejection.class);
        ConfigurationSerialization.registerClass(BuyRequest.class);
        ConfigurationSerialization.registerClass(DeletedShopClaim.class);
        ConfigurationSerialization.registerClass(LollipopNotification.class);
        ConfigurationSerialization.registerClass(SaleNotification.class);
        ConfigurationSerialization.registerClass(SaleNotificationAuto.class);
        ConfigurationSerialization.registerClass(SaleNotificationAutoClaim.class);
        ConfigurationSerialization.registerClass(SaleRejection.class);
        ConfigurationSerialization.registerClass(SellRequest.class);
    }
    
    @Override
    public void onDisable()
    {
        state.saveAll();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
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
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event)
    {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        loc.setY(loc.getY() + 1);
        if (state.getShop(loc) != null) {
            sendError(event.getPlayer(), "You cannot remove this block because there is a shop above it!");
            event.setCancelled(true); 
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
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
                int delta = event.getAction() == Action.LEFT_CLICK_BLOCK ? 1 : -1;
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
    public void onExplosion(EntityExplodeEvent event)
    {
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
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        ArrayDeque<Notification> p = state.getNotifications(event.getPlayer());
        if (!p.isEmpty()) {
            event.getPlayer().sendMessage(ChatColor.WHITE + "You have new notifications. Use " + Format.command("/shop notifications") + ChatColor.WHITE + " to view them");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event)
    {
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
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        String name = getConfig().getString("DeathTax.GoesTo", null);
        if (name != null) {
            Player pl = event.getEntity();
            if (econ.has(pl.getName(), 100.00) && isStupidDeath(event.getDeathMessage())) {
                double death_tax = econ.getBalance(pl.getName()) * getConfig().getDouble("DeathTax.Percentage", 0.04);
                econ.withdrawPlayer(pl.getName(), death_tax);
                econ.depositPlayer(name, death_tax);
                pl.sendMessage(String.format("You were fined %s for dying.", Format.money(death_tax)));
                if (getConfig().getBoolean("LogNotes", false)) {
                    log.info(Format.toAnsiColor(String.format("%s was fined %s for dying.", Format.username(pl.getName()), Format.money(death_tax))));
                }
            }
        }
    }
    
    private static boolean isStupidDeath(String death)
    {
        return (death.contains("fell") && !death.contains("world")) ||
               death.endsWith("drowned") ||
               death.endsWith("lava") ||
               death.contains("pricked") ||
               death.contains("suffocated");
    }
    
    public static StateFile getState()
    {
        return state;
    }
    
    /**
     * Sets up Vault.
     *
     * @return true on success, false otherwise
     */
    private boolean enableVault()
    {
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
     * Tests if two items are equal, ignoring quantities
     * @param stack1
     * @param stack2
     * @return 
     */
    public static boolean isItemEqual(ItemStack stack1, ItemStack stack2)
    {
        if (stack1 == null || stack2 == null) {
            return stack1 == stack2;
        }
        // cloning may be ineficient, but I don't want to modify the original objects
        ItemStack cloned1 = stack1.clone(),
                  cloned2 = stack2.clone();
        cloned1.setAmount(1);
        cloned2.setAmount(1);
        return cloned1.equals(cloned2);
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
            max      = item.getMaxStackSize();
        Inventory inv = pl.getInventory();
        ItemStack[] contents = inv.getContents();
        ItemStack curr;
        if (max == -1) {
            max = inv.getMaxStackSize();
        }
        for (int i = 0; i < contents.length; ++i) {
            if ((curr = contents[i]) == null || curr.getType() == Material.AIR) {
                quantity -= max;
                if (quantity <= 0) {
                    return true;
                }
                continue;
            }
            if (isItemEqual(curr, contents[i])) {
                quantity -= max - curr.getAmount();
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
        int amnt = item.getAmount(),
            max = item.getMaxStackSize();
        if (max == -1) {
            max = pl.getInventory().getMaxStackSize();
        }
        HashMap<Integer, ItemStack> overflow = new HashMap<>();
        if (amnt > max) {
            do {
                amnt -= max;
                if (amnt > 0) {
                    overflow = pl.getInventory().addItem(cloneAmnt(item, max));
                    if (!overflow.isEmpty()) {
                        return overflow.get(0).getAmount() + amnt;
                    }
                }
                else {
                    overflow = pl.getInventory().addItem(cloneAmnt(item, amnt + max));
                }
            }
            while(amnt > 0);
        }
        else {
            overflow = pl.getInventory().addItem(item);
        }
        if (overflow.isEmpty()) {
            return 0;
        }
        else {
            return overflow.get(0).getAmount();
        }
    }
    
    /**
     * Clones an ItemStack and sets the amount
     * @param item
     * @param amt
     * @return a cloned ItemStack
     */
    public static ItemStack cloneAmnt(ItemStack item, int amt)
    {
        ItemStack cloned = item.clone();
        cloned.setAmount(amt);
        return cloned;
    }
    
    /**
     * Checks if an ItemStack can be given to a player, then gives it
     * @param player
     * @param item
     * @return true if ItemStack was given, false otherwise
     */
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
    
    public void removeSelection(Player pl)
    {
        if (selectedShops.get(pl) != null) {
            selectedShops.remove(pl);
        }
    }
    
    /**
     * Tries to get around the imprecision of the double type
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
        sb.append(Format.toAnsiColor(message));
        log.info(sb.toString());
    }
}
