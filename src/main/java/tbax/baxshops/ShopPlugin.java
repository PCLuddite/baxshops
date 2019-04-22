/*
 * Copyright (C) 2013-2019 Timothy Baxendale
 * Portions derived from Shops Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package tbax.baxshops;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.commands.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.notification.*;
import tbax.baxshops.serialization.ItemNames;
import tbax.baxshops.serialization.SavedState;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class ShopPlugin extends JavaPlugin
{
    private static CommandMap commands;
    private static Economy econ;
    private static Logger log;
    private static SavedState savedState;
    private static String[] RAW_COMMANDS = { "buy", "sell", "restock", "restockall" };

    /**
     * A map containing each player's currently selected shop and other
     * selection data
     */
    private static Map<UUID, ShopSelection> selectedShops = new HashMap<>();

    private static CommandMap initCommands()
    {
        CommandMap commands = new CommandMap();
        try {
            commands.add(CmdAccept.class);
            commands.add(CmdAdd.class);
            commands.add(CmdBackup.class);
            commands.add(CmdBuy.class);
            commands.add(CmdClaim.class);
            commands.add(CmdCopy.class);
            commands.add(CmdCreate.class);
            commands.add(CmdDelete.class);
            commands.add(CmdEmpty.class);
            commands.add(CmdFlag.class);
            commands.add(CmdGiveXp.class);
            commands.add(CmdHelp.class);
            commands.add(CmdInfo.class);
            commands.add(CmdList.class);
            commands.add(CmdLollipop.class);
            commands.add(CmdNotifications.class);
            commands.add(CmdReject.class);
            commands.add(CmdReload.class);
            commands.add(CmdRemove.class);
            commands.add(CmdRestock.class);
            commands.add(CmdSave.class);
            commands.add(CmdSell.class);
            commands.add(CmdSet.class);
            commands.add(CmdSetAmnt.class);
            commands.add(CmdSetDur.class);
            commands.add(CmdSetIndex.class);
            commands.add(CmdSign.class);
            commands.add(CmdSkip.class);
            commands.add(CmdTake.class);
            commands.add(CmdTakeXp.class);
            commands.add(CmdTeleport.class);
            return commands;
        }
        catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, BaxShopCommand> getCommands()
    {
        return commands;
    }

    public static int showNotificationCount(Player player)
    {
        Deque<Notification> notifications = savedState.getNotifications(player);
        if (notifications.isEmpty()) {
            player.sendMessage("You have no new notifications");
            return 0;
        }
        else {
            int size = notifications.size();
            player.sendMessage(String.format("You have %s notification%s", Format.number(size), size == 1 ? "" : "s"));
            return size;
        }
    }

    /**
     * Shows a player his/her most recent notification
     *
     * @param player the player
     */
    public static void showNotification(Player player)
    {
        Deque<Notification> notifications = savedState.getNotifications(player);
        Notification n = notifications.getFirst();
        sendInfo(player, n.getMessage(player.getPlayer()));
        if (n instanceof Request) {
            player.sendMessage(String.format("Use %s or %s to manage this request.",
                Format.command("/shop accept"),
                Format.command("/shop reject"))
            );
        }
        else if (n instanceof Claimable) {
            player.sendMessage(String.format("Use %s to claim and remove this notification.",
                Format.command("/shop claim"))
            );
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
        sendNotification(player, n, getSavedState().getConfig().isLogNotes());
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
        Deque<Notification> ns = savedState.getNotifications(player);
        if (logNote) {
            log.info(Format.toAnsiColor(n.getMessage(null)));
        }
        ns.add(n);
        if (player.isOnline()) {
            if (ns.size() == 1) {
                showNotification(player.getPlayer());
            }
            else {
                showNotificationCount(player.getPlayer());
            }
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
        sendNotification(savedState.getOfflinePlayer(playerId), note);
    }

    public static void addShop(BaxShop shop)
    {
        savedState.addShop(shop);
    }

    public static void removeShop(UUID shopId)
    {
        savedState.removeShop(shopId);
    }

    public static void removeLocation(UUID shopId, Location loc)
    {
        savedState.removeLocation(shopId, loc);
    }

    public static @NotNull OfflinePlayer getOfflinePlayer(UUID uuid)
    {
        return savedState.getOfflinePlayer(uuid);
    }

    public static BaxShop getShop(UUID shopId)
    {
        return savedState.getShop(shopId);
    }

    public static @NotNull List<StoredPlayer> getOfflinePlayer(String playerName)
    {
        return savedState.getOfflinePlayer(playerName);
    }

    public static BaxShop getShop(Location location)
    {
        return savedState.getShop(location);
    }

    public static boolean addLocation(Location location, BaxShop shop)
    {
        return savedState.addLocation(shop, location);
    }

    public static boolean runCommand(ShopCmdActor actor)
    {
        if (actor.getNumArgs() == 0) {
            return false;
        }

        BaxShopCommand cmd = commands.get(actor.getAction());
        if (cmd == null) {
            actor.sendError(Resources.INVALID_SHOP_ACTION, actor.getAction());
            return true;
        }
        else if (cmd.hasAlternative(actor)) {
            try {
                cmd = cmd.getAlternative().newInstance();
                actor.setAction(cmd.getName());
            }
            catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return runCommand(cmd, actor);
    }

    public static boolean runCommand(BaxShopCommand cmd, ShopCmdActor actor)
    {
        try {
            if (!cmd.hasValidArgCount(actor)) {
                actor.sendMessage(cmd.getHelp(actor).toString());
            }
            else if(!cmd.hasPermission(actor)) {
                actor.sendError(Resources.NO_PERMISSION);
            }
            else if(cmd.requiresPlayer(actor) && actor.getPlayer() == null) {
                actor.sendError("You must be a player to use this command.");
            }
            else if(cmd.requiresSelection(actor) && actor.getShop() == null) {
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

    public static void sendInfo(@NotNull Player pl, String message)
    {
        pl.sendMessage(message);
        if (getSavedState().getConfig().isLogNotes()) {
            logPlayerMessage(pl, message);
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

    public static SavedState getSavedState()
    {
        return savedState;
    }

    public static void logInfo(String msg)
    {
        log.info(msg);
    }

    public static void logWarning(String msg)
    {
        log.warning(msg);
    }

    public static void logSevere(String msg)
    {
        log.severe(msg);
    }

    public static Collection<StoredPlayer> getRegisteredPlayers()
    {
        return savedState.getRegisteredPlayers();
    }

    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        log = getLogger();

        if (!enableVault()) {
            log.severe("BaxShops could not use this server's economy! Make sure Vault is installed!");
            getPluginLoader().disablePlugin(this);
            return;
        }

        if ((commands = initCommands()) == null) {
            log.severe("BaxShops failed to initialize its commands");
            getPluginLoader().disablePlugin(this);
            return;
        }

        loadConfigurationSerializable();

        ItemNames.loadDamageable(this);
        ItemNames.loadEnchants(this);

        saveDefaultConfig();
        savedState = SavedState.readFromDisk(this);

        // run an initial save 5 minutes after starting, then a recurring save
        // every 30 minutes after the first save
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, savedState::writeToDisk, 6000L, 36000L);
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
        ConfigurationSerialization.registerClass(SaleNotificationAuto.class);
        ConfigurationSerialization.registerClass(SaleNotificationAutoClaim.class);
        ConfigurationSerialization.registerClass(SaleRejection.class);
        ConfigurationSerialization.registerClass(SaleRequest.class);
        ConfigurationSerialization.registerClass(SellRequest.class);
        ConfigurationSerialization.registerClass(StoredPlayer.class);
    }

    @Override
    public void onDisable()
    {
        log.info("Saving BaxShops...");
        savedState.writeToDisk();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args)
    {
        ShopCmdActor actor = new ShopCmdActor(sender, command, args);

        if (actor.cmdIs(RAW_COMMANDS)) {
            actor.insertAction(actor.getCmdName());
            actor.setCmdName("shop");
        }

        return runCommand(actor);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
                                      @NotNull String[] args)
    {
        ShopCmdActor actor = new ShopCmdActor(sender, command, args);

        if (actor.cmdIs(RAW_COMMANDS)) {
            actor.insertAction(actor.getCmdName());
            actor.setCmdName("shop");
            args = actor.getArgs();
        }

        if (actor.getNumArgs() == 1) {
            return commands.entrySet().stream()
                .filter(c -> c.getKey().equals(c.getValue().getName())
                        && c.getValue().hasPermission(actor)
                        && c.getKey().contains(actor.getArg(0)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }
        else if (actor.getNumArgs() > 1) {
            BaxShopCommand cmd = commands.get(actor.getArg(0));
            if (cmd != null) {
                String arg = actor.getArg(actor.getNumArgs() - 1).toLowerCase();
                return cmd.onTabComplete(actor, command, alias, args).stream()
                    .filter(s -> s != null && s.toLowerCase().contains(arg))
                    .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
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
        return true;
    }
}
