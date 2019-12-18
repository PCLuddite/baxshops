/*
 * Copyright (C) Timothy Baxendale
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
package org.tbax.baxshops.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tbax.baxshops.*;
import org.tbax.baxshops.errors.CommandErrorException;
import org.tbax.baxshops.errors.CommandMessageException;
import org.tbax.baxshops.errors.CommandWarningException;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.*;
import org.tbax.baxshops.internal.items.ItemUtil;
import org.tbax.baxshops.serialization.StoredPlayer;
import org.tbax.baxshops.internal.versioning.LegacyPlayerUtil;

import java.util.*;

@SuppressWarnings("unused")
public final class ShopCmdActor implements CommandSender
{
    private final CommandSender sender;
    private final Command command;
    private String name;
    private String action;
    private String excluded;
    
    private String[] args;
    
    public ShopCmdActor(CommandSender sender, Command command, String[] args)
    {
        this.sender = sender;
        this.command = command;
        this.name = command.getName();
        setArgs(args);
    }

    public void setArgs(String[] args)
    {
        List<String> argList = new ArrayList<>(args.length);
        int idx = 0;
        for (; idx < args.length - 1; ++idx) {
            String arg = args[idx];
            if (Arrays.asList("-e", "--exclude", "--except").contains(arg)) {
                excluded = args[++idx];
            }
            else {
                argList.add(arg);
            }
        }
        if (idx < args.length)
            argList.add(args[idx]);
        this.args = argList.toArray(new String[0]);
    }

    public @Nullable List<BaxEntry> getExcluded() throws PrematureAbortException
    {
        if (excluded == null)
            return null;
        return Collections.singletonList(getEntryFromString(excluded, "Excluded item not found in shop"));
    }

    public String[] getArgs()
    {
        return args;
    }
    
    public CommandSender getSender()
    {
        return sender;
    }
    
    public Command getCommand()
    {
        return command;
    }

    public Player getPlayer()
    {
        try {
            return (Player) sender;
        }
        catch (ClassCastException e) {
            return null;
        }
    }

    public boolean isAdmin()
    {
        return sender.hasPermission(Permissions.SHOP_ADMIN);
    }

    public boolean isOwner()
    {
        ShopSelection selection = getSelection();
        return selection != null && selection.isOwner();
    }

    @Override
    public boolean isPermissionSet(@NotNull String permission)
    {
        return getSender().isPermissionSet(permission);
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission permission)
    {
        return getSender().isPermissionSet(permission);
    }

    public boolean hasPermission(@NotNull String permission)
    {
        return sender.hasPermission(permission);
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission)
    {
        return getSender().hasPermission(permission);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b)
    {
        return getSender().addAttachment(plugin, s, b);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin)
    {
        return getSender().addAttachment(plugin);
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b, int i)
    {
        return getSender().addAttachment(plugin, s, b, i);
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int i)
    {
        return getSender().addAttachment(plugin, i);
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment permissionAttachment)
    {
        getSender().removeAttachment(permissionAttachment);
    }

    @Override
    public void recalculatePermissions()
    {
        getSender().recalculatePermissions();
    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        return getSender().getEffectivePermissions();
    }

    public boolean cmdIs(String... names)
    {
        for (String name : names) {
            if (this.name.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    public @Nullable ShopSelection getSelection()
    {
        if (getPlayer() == null) {
            if (getSender() == Bukkit.getConsoleSender()) {
                return ShopPlugin.getSelection(StoredPlayer.DUMMY);
            }
            else {
                return null;
            }
        }
        else {
            return ShopPlugin.getSelection(getPlayer());
        }
    }
    
    public int getNumArgs()
    {
        return args.length;
    }
    
    public String getArg(int index)
    {
        return args[index];
    }

    public BaxQuantity getArgPlayerQty(int index)
    {
        return new BaxQuantity(args[index], getPlayer(), getPlayer().getInventory(), getItemInHand());
    }

    public BaxQuantity getArgShopQty(int index, BaxEntry entry) throws PrematureAbortException
    {
        if (getShop() == null)
            throw new CommandErrorException(Resources.NOT_FOUND_SELECTED);
        if (getShop().hasFlagInfinite() && (BaxQuantity.isAll(args[index]) || BaxQuantity.isMost(args[index])))
            throw new CommandErrorException("This shop has infinite supplies. You cannot take " + args[index].toLowerCase());
        return new BaxQuantity(args[index], getPlayer(), getShop().getItemStackInventory(), entry.toItemStack());
    }

    public boolean isArgQty(int index)
    {
        return BaxQuantity.isQuantity(args[index]);
    }

    public boolean isArgQtyNotAny(int index)
    {
        return BaxQuantity.isQuantityNotAny(args[index]);
    }

    public int getArgInt(int index) throws PrematureAbortException
    {
        return getArgInt(index, String.format("Expecting argument %d to be a whole number", index));
    }

    public int getArgInt(int index, String errMsg) throws PrematureAbortException
    {
        try {
            return Integer.parseInt(args[index]);
        }
        catch(NumberFormatException e) {
            throw new CommandErrorException(e, errMsg);
        }
    }

    public boolean isArgInt(int index)
    {
        try {
            Integer.parseInt(args[index]);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }

    public boolean isArgDouble(int index)
    {
        try {
            Double.parseDouble(args[index]);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }

    public double getArgRoundedDouble(int index) throws PrematureAbortException
    {
        return Math.round(100d * getArgDouble(index)) / 100d;
    }

    public double getArgRoundedDouble(int index, String errMsg) throws PrematureAbortException
    {
        return Math.round(100d * getArgDouble(index, errMsg)) / 100d;
    }

    public double getArgDouble(int index) throws PrematureAbortException
    {
        return getArgDouble(index, String.format("Expecting argument %d to be a number", index));
    }

    public double getArgDouble(int index, String errMsg) throws PrematureAbortException
    {
        try {
            return Double.parseDouble(args[index]);
        }
        catch (NumberFormatException e) {
            throw new CommandErrorException(e, errMsg);
        }
    }

    public short getArgShort(int index) throws PrematureAbortException
    {
        return getArgShort(index, String.format("Expecting argument %d to be a small whole number", index));
    }

    public short getArgShort(int index, String errMsg) throws PrematureAbortException
    {
        try {
            return Short.parseShort(args[index]);
        }
        catch (NumberFormatException e) {
            throw new CommandErrorException(e, errMsg);
        }
    }

    public boolean getArgBoolean(int index) throws PrematureAbortException
    {
        return getArgBoolean(index, String.format("Expecting argument %d to be yes/no", index));
    }

    public boolean getArgBoolean(int index, String errMsg) throws PrematureAbortException
    {
        if ("true".equalsIgnoreCase(args[index]) || "false".equalsIgnoreCase(args[index]))
            return "true".equalsIgnoreCase(args[index]);
        if ("yes".equalsIgnoreCase(args[index]) || "no".equalsIgnoreCase(args[index]))
            return "yes".equalsIgnoreCase(args[index]);
        if ("1".equalsIgnoreCase(args[index]) || "0".equalsIgnoreCase(args[index]))
            return "1".equalsIgnoreCase(args[index]);
        throw new CommandErrorException(errMsg);
    }

    public BaxEntry getArgEntry(int index) throws PrematureAbortException
    {
        return getArgEntry(index, Resources.NOT_FOUND_SHOPITEM);
    }

    public BaxEntry getArgEntry(int index, String errMsg) throws PrematureAbortException
    {
        return getEntryFromString(args[index], errMsg);
    }

    private BaxEntry getEntryFromString(String arg, String errMsg) throws PrematureAbortException
    {
        BaxEntry entry;
        try {
            entry = getShop().getEntry(Integer.parseInt(arg) - 1);
        }
        catch (NumberFormatException e) {
            List<BaxEntry> entries = ItemUtil.getItemFromAlias(arg, getShop());
            if (entries.size() == 0) {
                throw new CommandErrorException("No item with that name could be found");
            }
            else if (entries.size() > 1) {
                StringBuilder sb = new StringBuilder("There are multiple items that match that name:\n");
                for (BaxEntry baxEntry : entries) {
                    sb.append(baxEntry.getName()).append('\n');
                }
                throw new CommandErrorException(sb.toString());
            }
            else {
                return entries.get(0);
            }
        }
        catch (IndexOutOfBoundsException e) {
            throw new CommandErrorException(e, errMsg);
        }
        if (entry == null) {
            throw new CommandErrorException(errMsg);
        }
        return entry;
    }

    public int getArgEntryIndex(int index) throws PrematureAbortException
    {
        return getArgEntryIndex(index, Resources.NOT_FOUND_SHOPITEM);
    }

    public int getArgEntryIndex(int index, String errMsg) throws PrematureAbortException
    {
        return getShop().indexOf(getArgEntry(index, errMsg));
    }
    
    public BaxShop getShop()
    {
        if (getSelection() != null)
            return getSelection().getShop();
        return null;
    }
    
    public void setCmdName(String name)
    {
        this.name = name;
    }
    
    public String getCmdName()
    {
        return name;
    }
    
    /**
     * Gets the first argument (if present) in lower case
     * @return the first argument in lower case
     */
    public String getAction()
    {
        if (action == null) { // lazy initialization
            action = args.length > 0 ? args[0].toLowerCase() : "";
        }
        return action;
    }
    
    /**
     * Inserts a new first argument in the argument list
     * @param action the new first argument
     */
    public void insertAction(String action)
    {
        String[] newArgs = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = action;
        args = newArgs;
    }
    
    /**
     * Appends an argument to the end of the argument list
     * @param arg the argument to append
     */
    public void appendArg(Object arg)
    {
        appendArgs(arg);
    }

    public void appendArgs(Object... newArgs)
    {
        String[] allArgs = new String[args.length + newArgs.length];
        System.arraycopy(args, 0, allArgs, 0, args.length);
        for(int x = 0; x < newArgs.length; ++x) {
            allArgs[x + args.length] = newArgs[x].toString();
        }
        args = allArgs;
    }

    public void exitError(String format, Object... args) throws PrematureAbortException
    {
        throw new CommandErrorException(String.format(format, args));
    }

    public void sendError(String msg)
    {
        ShopPlugin.sendInfo(getSender(), ChatColor.RED + msg);
    }

    public void sendError(String format, Object... args)
    {
        ShopPlugin.sendInfo(getSender(), ChatColor.RED + String.format(format, args));
    }

    public void sendWarning(String msg)
    {
        ShopPlugin.sendInfo(getSender(), ChatColor.GOLD + msg);
    }

    public void sendWarning(String format, Object... args)
    {
        ShopPlugin.sendInfo(getSender(), ChatColor.GOLD + String.format(format, args));
    }

    public void exitWarning(String format, Object... args) throws PrematureAbortException
    {
        throw new CommandWarningException(String.format(format, args));
    }

    @Override
    public void sendMessage(@NotNull String msg)
    {
        ShopPlugin.sendInfo(getSender(), msg);
    }

    @Override
    public void sendMessage(@NotNull String[] strings)
    {
        ShopPlugin.sendInfo(getSender(), strings);
    }

    @Override
    public @NotNull Server getServer()
    {
        return getSender().getServer();
    }

    @Override
    public @NotNull String getName()
    {
        return getSender().getName();
    }

    public void sendMessage(String format, Object... args)
    {
        sendMessage(String.format(format, args));
    }

    public void exitMessage(String format, Object... args) throws PrematureAbortException
    {
        throw new CommandMessageException(String.format(format, args));
    }

    public void logError(String format, Object... args)
    {
        ShopPlugin.logSevere(String.format(format, args));
    }

    public void logWarning(String format, Object... args)
    {
        ShopPlugin.logWarning(String.format(format, args));
    }

    public void logMessage(String format, Object... args)
    {
        ShopPlugin.logInfo(String.format(format, args));
    }

    public ItemStack getItemInHand()
    {
        if (getPlayer() == null)
            return null;
        ItemStack item = LegacyPlayerUtil.getItemInHand(getPlayer());
        if (item == null || item.getType() == Material.AIR)
            return null;
        return item;
    }

    public void setItemInHand(ItemStack stack)
    {
        LegacyPlayerUtil.setItemInHand(getPlayer(), stack);
    }

    public List<BaxEntry> takeArgFromInventory(int index) throws PrematureAbortException
    {
        return PlayerUtil.takeQtyFromInventory(getArgPlayerQty(index), getShop(), getExcluded());
    }

    public List<BaxEntry> peekArgFromInventory(int index) throws PrematureAbortException
    {
        return PlayerUtil.peekQtyFromInventory(getArgPlayerQty(index), getShop(), getExcluded());
    }

    public PlayerInventory getInventory()
    {
        if (getPlayer() == null)
            return null;
        return getPlayer().getInventory();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(name);
        for (String s : args) {
            sb.append(" ");
            sb.append(s);
        }
        return sb.toString();
    }

    public void setArg(int index, Object value)
    {
        args[index] = value.toString();
    }

    public int giveItem(ItemStack item) throws PrematureAbortException
    {
        return PlayerUtil.giveItem(getPlayer(), item);
    }

    public int giveItem(ItemStack item, boolean allOrNothing) throws PrematureAbortException
    {
        return PlayerUtil.giveItem(getPlayer(), item, allOrNothing);
    }

    public int getSpaceForItem(ItemStack stack)
    {
        return PlayerUtil.getSpaceForItem(getPlayer(), stack);
    }

    public boolean hasRoomForItem(ItemStack stack)
    {
        return PlayerUtil.hasRoomForItem(getPlayer(), stack);
    }

    public boolean tryGiveItem(ItemStack stack)
    {
        return PlayerUtil.tryGiveItem(getPlayer(), stack);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean isArgUuid(int index)
    {
        try {
            UUID.fromString(args[index]);
            return true;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    public UUID getArgUuid(int index) throws PrematureAbortException
    {
        return getArgUuid(index, String.format("Expecting argument %d to be a UUID", index));
    }

    public UUID getArgUuid(int index, String errMsg) throws PrematureAbortException
    {
        try {
            return UUID.fromString(args[index]);
        }
        catch (IllegalArgumentException e) {
            throw new CommandErrorException(e, errMsg);
        }
    }

    public StoredPlayer getArgPlayer(int index) throws PrematureAbortException
    {
        try {
            return ShopPlugin.getState().getOfflinePlayer(getArgUuid(index));
        }
        catch (PrematureAbortException e){
            List<StoredPlayer> players = ShopPlugin.getOfflinePlayer(args[index]);
            if (players.isEmpty()) {
                return null;
            }
            else if (players.size() > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append("There are multiple players with that name. Please specify the UUID instead.\n");
                for (StoredPlayer player : players) {
                    sb.append(Format.keyword(player.getUniqueId().toString())).append('\n');
                }
                exitError(sb.toString());
            }
            return players.get(0);
        }
    }

    public StoredPlayer getArgPlayerSafe(int index) throws PrematureAbortException
    {
        StoredPlayer player = getArgPlayer(index);
        if (player == null)
            return ShopPlugin.getOfflinePlayerSafe(args[index]).get(0);
        if (player == StoredPlayer.ERROR)
            return ShopPlugin.getOfflinePlayer(getArgUuid(index));
        return player;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    @Override
    public boolean isOp()
    {
        return getSender().isOp();
    }

    @Override
    public void setOp(boolean b)
    {
        getSender().setOp(b);
    }

    public StoredPlayer getStoredPlayer()
    {
        return ShopPlugin.getState().getOfflinePlayer(getPlayer().getUniqueId());
    }
}
