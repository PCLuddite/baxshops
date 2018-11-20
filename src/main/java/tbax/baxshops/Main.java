/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.commands.*;
import tbax.baxshops.notification.*;
import tbax.baxshops.serialization.StateFile;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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

    private static final CommandMap commands = initCommands();
    
    /**
     * The Vault economy
     */
    private static Economy econ;
    
    private static Logger log;
    
    public Main()
    {
    }

    public static Map<String, BaxShopCommand> getCommands()
    {
        return commands;
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

    private static CommandMap initCommands()
    {
		return new CommandMap(
			CmdAccept.class,
			CmdAdd.class,
			CmdAccept.class,
			CmdAdd.class,
			CmdBackup.class,
			CmdBuy.class,
			CmdCopy.class,
			CmdCreate.class,
			CmdFlag.class,
			CmdGiveXp.class,
			CmdHelp.class,
			CmdList.class,
			CmdLollipop.class,
			CmdNotifications.class,
			CmdReject.class,
			CmdRestock.class,
			CmdSave.class,
			CmdSell.class,
			CmdSet.class,
			CmdSkip.class,
			CmdTakeXp.class,
			CmdTeleport.class
		);
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
    
    public void removeSelection(Player pl)
    {
        if (selectedShops.get(pl) != null) {
            selectedShops.remove(pl);
        }
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
