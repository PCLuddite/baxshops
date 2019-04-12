/*
 * Copyright (C) 2013-2019 Timothy Baxendale
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
package tbax.baxshops.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.CommandHelpArgument;
import tbax.baxshops.Resources;
import tbax.baxshops.commands.flags.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CmdFlag extends BaxShopCommand
{
    private static final CommandMap flagCmds = new CommandMap();

    static {
        try {
            flagCmds.add(FlagCmdBuyRequests.class);
            flagCmds.add(FlagCmdInfinite.class);
            flagCmds.add(FlagCmdList.class);
            flagCmds.add(FlagCmdNotify.class);
            flagCmds.add(FlagCmdOwner.class);
            flagCmds.add(FlagCmdSellRequests.class);
            flagCmds.add(FlagCmdSellToShop.class);
        }
        catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull String getName()
    {
        return "flag";
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("Set a specific flag or list all flags applied to a selected shop");
        help.setArgs(
            new CommandHelpArgument("name|list", "the name of the flag to set or a list of all flags currently applied to this shop", true),
            new CommandHelpArgument("setting", "the value this flag should be set to", false)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        if (actor.getNumArgs() < 2)
            return false;
        BaxShopCommand command = flagCmds.get(actor.getArg(1));
        return command != null && command.hasValidArgCount(actor);
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        if (actor.getNumArgs() < 2)
            return false;
        BaxShopCommand command = flagCmds.get(actor.getArg(1));
        return command != null && command.requiresSelection(actor);
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        if (actor.getNumArgs() < 2)
            return false;
        BaxShopCommand command = flagCmds.get(actor.getArg(1));
        return command != null && command.requiresOwner(actor);
    }

    @Override
    public boolean requiresPlayer(@NotNull ShopCmdActor actor)
    {
        if (actor.getNumArgs() < 2)
            return false;
        BaxShopCommand command = flagCmds.get(actor.getArg(1));
        return command != null && command.requiresPlayer(actor);
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        if (actor.getNumArgs() < 2)
            return false;
        BaxShopCommand command = flagCmds.get(actor.getArg(1));
        return command != null && command.requiresItemInHand(actor);
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
		FlagCmd flagCmd = (FlagCmd)flagCmds.get(actor.getArg(1));
		if (flagCmd.requiresRealOwner(actor) && actor.getShop() != null && StoredPlayer.DUMMY.equals(actor.getShop().getOwner())) {
		    actor.exitError(Resources.PLAYER_NO_NOTES, actor.getShop().getOwner());
        }
		flagCmd.onCommand(actor);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        ShopCmdActor actor = (ShopCmdActor)sender;
        if (args.length == 2) {
            return flagCmds.entrySet().stream()
                .filter(c -> c.getKey().equals(c.getValue().getName()) && c.getValue().hasPermission(actor))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }
        else if (args.length > 2) {
            FlagCmd flagCmd = (FlagCmd)flagCmds.get(actor.getArg(1));
            if (flagCmd != null) {
                return flagCmd.onTabComplete(actor, command, alias, args);
            }
        }
        return super.onTabComplete(actor, command, alias, args);
    }
}
