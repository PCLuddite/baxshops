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

import tbax.baxshops.help.Help;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import tbax.baxshops.executer.*;
import tbax.baxshops.notification.*;
import tbax.baxshops.serialization.StateFile;

public final class Main extends JavaPlugin
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
    private static Economy econ;
    
    private static Logger log;
    
    public Main()
    {
    }

    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
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
    
    public static StateFile getState()
    {
        return state;
    }
    
    public static Logger getLog()
    {
        return log;
    }
    
    public static Economy getEconomy()
    {
        return econ;
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
            sender.sendMessage(ChatColor.RED + message);
        }
    }
    
    /**
     * Warns a player with a message
     *
     * @param sender the player
     * @param message the error message
     */
    public static void sendWarning(CommandSender sender, String message)
    {
        if (sender != null) {
            sender.sendMessage(ChatColor.GOLD + message);
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
        if (max == -1) {
            max = inv.getMaxStackSize();
        }
        for (ItemStack curr : contents) {
            if (curr == null || curr.getType() == Material.AIR) {
                quantity -= max;
                if (quantity <= 0) {
                    return true;
                }
            }
            else if (isItemEqual(curr, item)) {
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

    public static List<String> getSignText(Location loc)
    {
        try {
            Sign sign = (Sign) loc.getBlock().getState();
            String[] allLines = sign.getLines();
            int emptylines = 0;
            for(int i = allLines.length - 1; i >= 0; --i) {
                if (allLines[i].isEmpty()) {
                    ++emptylines;
                }
                else {
                    break;
                }
            }
            if (emptylines == allLines.length) {
                return new ArrayList<>();
            }
            int start = 0;
            while(allLines[start].isEmpty()) {
                ++start;
            }
            ArrayList<String> lines = new ArrayList<>();
            for(int i = start; i < allLines.length - emptylines; ++i) {
                lines.add(allLines[i]);
            }
            return lines;
        } catch (ClassCastException e) {
            return new ArrayList<>();
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
