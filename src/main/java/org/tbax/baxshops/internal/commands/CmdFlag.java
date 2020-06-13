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
package org.tbax.baxshops.internal.commands;

import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.CommandHelpArgument;
import org.tbax.baxshops.commands.BaxCommand;
import org.tbax.baxshops.commands.CmdActor;
import org.tbax.baxshops.commands.CommandArgument;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.commands.flags.*;
import org.tbax.baxshops.serialization.StoredPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CmdFlag extends ShopCommand
{
    private static final CommandMap flagCmds = new CommandMap();

    static {
        try {
            flagCmds.add(FlagCmdBuyRequests.class);
            flagCmds.add(FlagCmdInfinite.class);
            flagCmds.add(FlagCmdList.class);
            flagCmds.add(FlagCmdSellRequests.class);
            flagCmds.add(FlagCmdSellToShop.class);
            flagCmds.add(FlagCmdSmartStack.class);
        }
        catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "flag";
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "manage shop flags");
        StringBuilder description = new StringBuilder("Set a specific flag or list all flags applied to a selected shop.\n");
        description.append("\nThe following flags are available:\n");

        List<FlagCmd> flags = flagCmds.values().stream()
                .filter(cmd -> cmd.hasPermission(actor))
                .distinct()
                .sorted(Comparator.comparing(BaxCommand::getAction))
                .map(cmd -> (FlagCmd)cmd)
                .collect(Collectors.toList());

        for (FlagCmd cmd : flags) {
            description.append("\n");
            description.append(cmd.getAction()).append(": ").append(cmd.getHelp(actor).getShortDescription());
        }

        help.setLongDescription(description.toString());
        help.setArgs(
                new CommandHelpArgument("option", "the name of the flag to set or use 'list' of all flags currently applied to this shop", true),
                new CommandHelpArgument("true/false", "the value this flag should be set to", false)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        if (actor.getNumArgs() < 2)
            return false;
        BaxCommand command = flagCmds.get(actor.getArg(1).asString());
        return command != null && command.hasValidArgCount(actor);
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        if (actor.getNumArgs() < 2)
            return false;
        ShopCommand command = flagCmds.get(actor.getArg(1).asString());
        return command != null && command.requiresSelection(actor);
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        if (actor.getNumArgs() < 2)
            return false;
        ShopCommand command = flagCmds.get(actor.getArg(1).asString());
        return command != null && command.requiresOwner(actor);
    }

    @Override
    public boolean requiresPlayer(@NotNull CmdActor actor)
    {
        if (actor.getNumArgs() < 2)
            return false;
        BaxCommand command = flagCmds.get(actor.getArg(1).asString());
        return command != null && command.requiresPlayer(actor);
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        if (actor.getNumArgs() < 2)
            return false;
        ShopCommand command = flagCmds.get(actor.getArg(1).asString());
        return command != null && command.requiresItemInHand(actor);
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        FlagCmd flagCmd = (FlagCmd)flagCmds.get(actor.getArg(1).asString());
        if (flagCmd.requiresRealOwner(actor) && actor.getShop() != null && StoredPlayer.DUMMY.equals(actor.getShop().getOwner())) {
            actor.exitError("%s is not a real player and cannot receive notifications.\nThe value of this flag cannot be changed.", actor.getShop().getOwner());
        }
        if (actor.hasPermission(flagCmd.getPermission())) {
            flagCmd.onCommand(actor);
        }
        else {
            actor.sendError("You are not permitted to change this flag");
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<? extends CommandArgument> args)
    {
        if (args.size() == 2) {
            return flagCmds.entrySet().stream()
                    .filter(c -> c.getKey().equals(c.getValue().getAction()) && c.getValue().hasPermission(actor))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }
        else if (args.size() > 2) {
            FlagCmd flagCmd = (FlagCmd)flagCmds.get(actor.getArg(1).asString());
            if (flagCmd != null) {
                return flagCmd.onTabComplete(actor, command, alias, args);
            }
        }
        return super.onTabComplete(actor, command, alias, args);
    }
}