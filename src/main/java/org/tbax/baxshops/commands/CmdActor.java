/*
 * Copyright (C) Timothy Baxendale
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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxQuantity;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.PlayerUtil;
import org.tbax.baxshops.errors.CommandErrorException;
import org.tbax.baxshops.errors.CommandMessageException;
import org.tbax.baxshops.errors.CommandWarningException;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.internal.versioning.LegacyPlayerUtil;
import org.tbax.baxshops.serialization.StoredPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public interface CmdActor extends CommandSender
{
    void setArgs(String[] args);
    String[] getArgs();
    CommandSender getSender();
    Command getCommand();
    Player getPlayer();
    boolean isAdmin();
    int getNumArgs();

    default boolean cmdIs(String... names)
    {
        String name = getCmdName();
        for (String testName : names) {
            if (testName.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }
    String getArg(int index);

    default BaxQuantity getArgPlayerQty(int index)
    {
        return new BaxQuantity(getArg(index), getPlayer(), getPlayer().getInventory(), getItemInHand());
    }

    default boolean isArgQty(int index)
    {
        return BaxQuantity.isQuantity(getArg(index));
    }

    default boolean isArgQtyNotAny(int index)
    {
        return BaxQuantity.isQuantityNotAny(getArg(index));
    }

    default int getArgInt(int index) throws PrematureAbortException
    {
        return getArgInt(index, String.format("Expecting argument %d to be a whole number", index));
    }

    default int getArgInt(int index, String errMsg) throws PrematureAbortException
    {
        try {
            return Integer.parseInt(getArg(index));
        }
        catch(NumberFormatException e) {
            throw new CommandErrorException(e, errMsg);
        }
    }

    default boolean isArgInt(int index)
    {
        try {
            Integer.parseInt(getArg(index));
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }

    default boolean isArgDouble(int index)
    {
        try {
            Double.parseDouble(getArg(index));
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }

    default double getArgRoundedDouble(int index) throws PrematureAbortException
    {
        return Math.round(100d * getArgDouble(index)) / 100d;
    }

    default double getArgRoundedDouble(int index, String errMsg) throws PrematureAbortException
    {
        return Math.round(100d * getArgDouble(index, errMsg)) / 100d;
    }

    default double getArgDouble(int index) throws PrematureAbortException
    {
        return getArgDouble(index, String.format("Expecting argument %d to be a number", index));
    }

    default double getArgDouble(int index, String errMsg) throws PrematureAbortException
    {
        try {
            return Double.parseDouble(getArg(index));
        }
        catch (NumberFormatException e) {
            throw new CommandErrorException(e, errMsg);
        }
    }

    default short getArgShort(int index) throws PrematureAbortException
    {
        return getArgShort(index, String.format("Expecting argument %d to be a small whole number", index));
    }

    default short getArgShort(int index, String errMsg) throws PrematureAbortException
    {
        try {
            return Short.parseShort(getArg(index));
        }
        catch (NumberFormatException e) {
            throw new CommandErrorException(e, errMsg);
        }
    }

    default boolean getArgBoolean(int index) throws PrematureAbortException
    {
        return getArgBoolean(index, String.format("Expecting argument %d to be yes/no", index));
    }

    default boolean getArgBoolean(int index, String errMsg) throws PrematureAbortException
    {
        String arg = getArg(index);
        if ("true".equalsIgnoreCase(arg) || "false".equalsIgnoreCase(arg))
            return "true".equalsIgnoreCase(arg);
        if ("yes".equalsIgnoreCase(arg) || "no".equalsIgnoreCase(arg))
            return "yes".equalsIgnoreCase(arg);
        if ("1".equalsIgnoreCase(arg) || "0".equalsIgnoreCase(arg))
            return "1".equalsIgnoreCase(arg);
        throw new CommandErrorException(errMsg);
    }

    default String getArgEnum(int index, String... args) throws PrematureAbortException
    {
        return getArgEnum(index, Arrays.asList(args));
    }

    default String getArgEnum(int index, List<String> args) throws PrematureAbortException
    {
        String arg = getArg(index);
        for (String a : args) {
            if (a.equalsIgnoreCase(arg)) {
                return a;
            }
        }
        throw new CommandErrorException("'" + arg + "' must be either " + Format.listOr(args));
    }
    
    void setCmdName(String name);
    String getCmdName();
    String getAction();
    void setAction(String action);
    
    /**
     * Inserts a new first argument in the argument list
     * @param action the new first argument
     */
    void insertAction(String action);
    
    /**
     * Appends an argument to the end of the argument list
     * @param arg the argument to append
     */
    void appendArg(Object arg);
    void appendArgs(Object... newArgs);

    default void exitError(String format, Object... args) throws PrematureAbortException
    {
        throw new CommandErrorException(String.format(format, args));
    }

    default void sendError(String msg)
    {
        ShopPlugin.sendInfo(getSender(), ChatColor.RED + msg);
    }

    default void sendError(String format, Object... args)
    {
        ShopPlugin.sendInfo(getSender(), ChatColor.RED + String.format(format, args));
    }

    default void sendWarning(String msg)
    {
        ShopPlugin.sendInfo(getSender(), ChatColor.GOLD + msg);
    }

    default void sendWarning(String format, Object... args)
    {
        ShopPlugin.sendInfo(getSender(), ChatColor.GOLD + String.format(format, args));
    }

    default void exitWarning(String format, Object... args) throws PrematureAbortException
    {
        throw new CommandWarningException(String.format(format, args));
    }

    @Override
    default void sendMessage(@NotNull String msg)
    {
        ShopPlugin.sendInfo(getSender(), msg);
    }

    @Override
    default void sendMessage(@NotNull String[] strings)
    {
        ShopPlugin.sendInfo(getSender(), strings);
    }

    default void sendMessage(String format, Object... args)
    {
        sendMessage(String.format(format, args));
    }

    default void exitMessage(String format, Object... args) throws PrematureAbortException
    {
        throw new CommandMessageException(String.format(format, args));
    }

    default void logError(String format, Object... args)
    {
        ShopPlugin.logSevere(String.format(format, args));
    }

    default void logWarning(String format, Object... args)
    {
        ShopPlugin.logWarning(String.format(format, args));
    }

    default void logMessage(String format, Object... args)
    {
        ShopPlugin.logInfo(String.format(format, args));
    }

    default ItemStack getItemInHand()
    {
        if (getPlayer() == null)
            return null;
        ItemStack item = LegacyPlayerUtil.getItemInHand(getPlayer());
        if (item == null || item.getType() == Material.AIR)
            return null;
        return item;
    }

    default void setItemInHand(ItemStack stack)
    {
        LegacyPlayerUtil.setItemInHand(getPlayer(), stack);
    }

    default PlayerInventory getInventory()
    {
        if (getPlayer() == null)
            return null;
        return getPlayer().getInventory();
    }

    void setArg(int index, Object value);

    default int giveItem(ItemStack item) throws PrematureAbortException
    {
        return PlayerUtil.giveItem(getPlayer(), item);
    }

    default int giveItem(ItemStack item, boolean allOrNothing) throws PrematureAbortException
    {
        return PlayerUtil.giveItem(getPlayer(), item, allOrNothing);
    }

    default int getSpaceForItem(ItemStack stack)
    {
        return PlayerUtil.getSpaceForItem(getPlayer(), stack);
    }

    default boolean hasRoomForItem(ItemStack stack)
    {
        return PlayerUtil.hasRoomForItem(getPlayer(), stack);
    }

    default boolean tryGiveItem(ItemStack stack)
    {
        return PlayerUtil.tryGiveItem(getPlayer(), stack);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    default boolean isArgUuid(int index)
    {
        try {
            UUID.fromString(getArg(index));
            return true;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    default UUID getArgUuid(int index) throws PrematureAbortException
    {
        return getArgUuid(index, String.format("Expecting argument %d to be a UUID", index));
    }

    default UUID getArgUuid(int index, String errMsg) throws PrematureAbortException
    {
        try {
            return UUID.fromString(getArg(index));
        }
        catch (IllegalArgumentException e) {
            throw new CommandErrorException(e, errMsg);
        }
    }

    default OfflinePlayer getArgPlayer(int index) throws PrematureAbortException
    {
        try {
            return ShopPlugin.getState().getOfflinePlayer(getArgUuid(index));
        }
        catch (PrematureAbortException e){
            List<StoredPlayer> players = ShopPlugin.getOfflinePlayer(getArg(index));
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

    default OfflinePlayer getArgPlayerSafe(int index) throws PrematureAbortException
    {
        OfflinePlayer player = getArgPlayer(index);
        if (player == null)
            return ShopPlugin.getOfflinePlayerSafe(getArg(index)).get(0);
        if (player == StoredPlayer.ERROR)
            return ShopPlugin.getOfflinePlayer(getArgUuid(index, "The player could not be found"));
        return player;
    }
}
