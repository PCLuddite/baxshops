/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import tbax.baxshops.commands.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.notification.*;
import tbax.baxshops.serialization.StoredData;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public final class ShopPlugin extends JavaPlugin
{
    private static final CommandMap commands = initCommands();
    
    /**
     * The Vault economy
     */
    private static Economy econ;

    private static ShopPlugin plugin;
    private static Logger log;
    /**
     * A map containing each player's currently selected shop and other
     * selection data
     */
    private static Map<UUID, ShopSelection> selectedShops = new HashMap<>();

    public ShopPlugin()
    {
        plugin = this; // gross
    }

    public static Map<String, BaxShopCommand> getCommands()
    {
        return commands;
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
        Deque<Notification> notifications = StoredData.getNotifications(player.getPlayer());
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
    public static void sendNotification(String playerName, Notification n) throws PrematureAbortException
    {
        sendNotification(StoredData.getOfflinePlayer(playerName), n);
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
        Deque<Notification> ns = StoredData.getNotifications(player);
        if (logNote) {
            log.info(Format.toAnsiColor(n.getMessage(null)));
        }
        ns.add(n);
        if (player != null && player.isOnline()) {
            showNotification(player.getPlayer(), false);
        }
    }

    public static ShopSelection getSelection(Player player)
    {
        ShopSelection selected = selectedShops.get(player.getUniqueId());
        if (selected == null) {
            selected = new ShopSelection();
            selectedShops.put(player.getUniqueId(), selected);
        }
        return selected;
    }

    public static void clearSelection(Player player)
    {
        selectedShops.remove(player.getUniqueId());
    }

    public static void sendNotification(UUID playerId, Notification note)
    {
        sendNotification(StoredData.getOfflinePlayer(playerId), note);
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

        StoredData.load(this);
        
        saveDefaultConfig();
        
        // run an initial save 5 minutes after starting, then a recurring save
        // every 30 minutes after the first save
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                StoredData.saveAll();
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
        ConfigurationSerialization.registerClass(NoteSet.class);
        ConfigurationSerialization.registerClass(SaleClaim.class);
        ConfigurationSerialization.registerClass(SaleNotification.class);
        ConfigurationSerialization.registerClass(SaleRejection.class);
        ConfigurationSerialization.registerClass(SaleRequest.class);
        ConfigurationSerialization.registerClass(StoredPlayer.class);
    }
    
    @Override
    public void onDisable()
    {
        StoredData.saveAll();
    }

    private static CommandMap initCommands()
    {
		return new CommandMap(
			CmdAccept.class,
            CmdAdd.class,
            CmdBackup.class,
            CmdBuy.class,
            CmdCopy.class,
            CmdCreate.class,
            CmdDelete.class,
            CmdFlag.class,
            CmdGiveXp.class,
            CmdHelp.class,
            CmdInfo.class,
            CmdList.class,
            CmdLollipop.class,
            CmdNotifications.class,
            CmdReject.class,
            CmdRemove.class,
            CmdRestock.class,
            CmdSave.class,
            CmdSell.class,
            CmdSet.class,
            CmdSetAmnt.class,
            CmdSetAngle.class,
            CmdSetDur.class,
            CmdSetIndex.class,
            CmdSign.class,
            CmdSkip.class,
            CmdTake.class,
            CmdTakeXp.class,
            CmdTeleport.class
		);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        ShopCmdActor actor = new ShopCmdActor(sender, command, args);
        
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
        try {
            if (cmd == null) {
                actor.sendMessage(Resources.INVALID_SHOP_ACTION, actor.getAction());
            }
            if (!cmd.hasValidArgCount(actor)) {
                actor.sendMessage(cmd.getHelp(actor).toString());
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
            else if(cmd.requiresOwner(actor) && !(actor.isOwner() || actor.isAdmin())) {
                actor.sendError("You must be the owner of the shop to use /shop %s", actor.getAction());
            }
            else if(cmd.requiresItemInHand(actor) && actor.getItemInHand() == null) {
                actor.sendError(Resources.NOT_FOUND_HELDITEM);
            }
            else {
                cmd.onCommand(actor);
            }
        }
        catch(PrematureAbortException e) {
            actor.getSender().sendMessage(e.getMessage());
        }
        return true;
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

    public static ShopPlugin getInstance()
    {
        return plugin;
    }
}
