/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops;

import tbax.baxshops.commands.*;
import tbax.baxshops.help.Help;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
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

    private static final Map<String, BaxShopCommand> commands = initCommands();
    
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

    private static Map<String, BaxShopCommand> initCommands()
    {
        List<BaxShopCommand> cmds = new ArrayList<>();
        cmds.add(new CmdAccept());
        cmds.add(new CmdAdd());
        cmds.add(new CmdBackup());
        cmds.add(new CmdBuy());
        cmds.add(new CmdCopy());
        cmds.add(new CmdCreate());
        cmds.add(new CmdFlag());
        cmds.add(new CmdGiveXp());
        cmds.add(new CmdHelp());
        cmds.add(new CmdList());
        cmds.add(new CmdLollipop());
        cmds.add(new CmdNotifications());
        cmds.add(new CmdReject());
        cmds.add(new CmdRestock());
        cmds.add(new CmdSave());
        cmds.add(new CmdSell());
        cmds.add(new CmdSet());
        cmds.add(new CmdSkip());
        cmds.add(new CmdTakeXp());
        cmds.add(new CmdTeleport());

        HashMap<String, BaxShopCommand> map = new HashMap<>();
        for(BaxShopCommand cmd : cmds) {
            for(String alias : cmd.getAliases()) {
                map.put(alias, cmd);
            }
        }
        return map;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        ShopCmdActor actor = new ShopCmdActor(this, sender, command, args);
        
        if (actor.cmdIs("buy", "sell", "restock", "restockall")) {
            actor.insertAction(actor.getCmdName());
            actor.setCmdName("shop");
        }

        return runCommand(actor);
    }

    public static boolean runCommand(ShopCmdActor actor)
    {
        if (actor.getNumArgs() == 0) {
            Help.showHelp(actor.getSender());
            return false;
        }

        BaxShopCommand cmd = commands.get(actor.getAction());
        if (cmd == null) {
            actor.sendMessage(Resources.INVALID_SHOP_ACTION, actor.getAction());
        }
        if (!cmd.hasValidArgCount(actor)) {
            actor.sendMessage(cmd.getHelp().toString());
        }
        else if(!cmd.hasPermission(actor)) {
            actor.sendError(Resources.NO_PERMISSION);
        }
        else if(cmd.requiresPlayer(actor) && actor.getPlayer() == null) {
            actor.sendError("You must be a player to use this command.");
        }
        else if(cmd.requiresSelection(actor) && actor.getSelection() == null) {
            actor.sendError(Resources.NOT_FOUND_SELECTED);
        }
        else if(cmd.requiresOwner(actor) && !actor.isOwner()) {
            actor.sendError("You must be the owner of the shop to use /shop %s", actor.getAction());
        }
        else if(cmd.requiresItemInHand(actor) && actor.getItemInHand() == null) {
            actor.sendError(Resources.NOT_FOUND_HELDITEM);
        }
        else {
            try {
                cmd.onCommand(actor);
            }
            catch(PrematureAbortException e) {
                actor.getSender().sendMessage(e.getMessage());
            }
        }
        return true;
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
            else if (curr.isSimilar(item)) {
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
    
    /**
     * Removes a set number of items from an inventory
     * @param pl
     * @param entry
     * @param count
     * @return 
     */
    public static int clearItems(Player pl, BaxEntry entry, int count)
    {
        Inventory inv = pl.getInventory();
        int i = 0;
        int addSize = 0;
        
        while (i < inv.getSize()) {
        
            if (pl.getInventory().getItemInMainHand() != null) {
                if (pl.getInventory().getItemInMainHand().isSimilar(entry.getItemStack())) {
                    addSize += pl.getInventory().getItemInMainHand().getAmount();
                    if (addSize > count) {
                        int leftover = addSize - count;
                        if (leftover > 0) {
                            pl.getInventory().getItemInMainHand().setAmount(leftover);
                        }
                        return count;
                    }
                    else {
                        pl.getInventory().setItemInMainHand(null);
                    }
                }
            }
            
            if(inv.getItem(i).isSimilar(entry.getItemStack())) {
                ItemStack stack = inv.getItem(i);
                addSize += stack.getAmount();
                
                if (addSize > count) {
                    int leftover = addSize - count;
                    if (leftover > 0) {
                        stack.setAmount(leftover);
                    }
                    return count;
                }
                else {
                    inv.clear(i);
                }
            }
            i++;
        }
        return addSize;
    }
    
    /**
     * Removes from an inventory a single item
     * @param pl
     * @param entry
     * @return The number of items that have been removed
     */
    public static int clearItems(Player pl, BaxEntry entry)
    {
        ArrayList<BaxEntry> entries = new ArrayList<>();
        entries.add(entry);
        ArrayList<ItemStack> list = clearItems(pl, entries);
        if (list.isEmpty()) {
            return 0;
        }
        else {
            int num = 0;
            for(ItemStack stack : list) {
                num += stack.getAmount();
            }
            return num;
        }
    }
    
    /**
     * Removes from an inventory all items in the the List&lt;BaxEntry&gt;
     * @param pl
     * @param entries
     * @return The items that have been removed. Each ItemStack is a different item type and may exceed the material's max stack.
     */
    public static ArrayList<ItemStack> clearItems(Player pl, List<BaxEntry> entries)
    {
        ArrayList<ItemStack> removedItems = new ArrayList<>();
        Inventory inv = pl.getInventory();
        for(BaxEntry entry : entries) {
        
            int i = 0;
            int addSize = 0;
        
            if (pl.getInventory().getItemInMainHand() != null) {
                if (pl.getInventory().getItemInMainHand().isSimilar(entry.getItemStack())) {
                    addSize += pl.getInventory().getItemInMainHand().getAmount();
                    pl.getInventory().setItemInMainHand(null);
                }
            }
            
            while (i < inv.getSize()){
                if(inv.getItem(i).isSimilar(entry.getItemStack())) {
                    addSize += inv.getItem(i).getAmount();
                    inv.setItem(i, null);
                }
                i++;
            }
            if (addSize > 0) {
                ItemStack stack = entry.toItemStack();
                stack.setAmount(addSize);
                removedItems.add(stack);
            }
        }
        return removedItems;
    }
}
