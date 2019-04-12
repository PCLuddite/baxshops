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

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.List;
import java.util.stream.Collectors;

public final class CmdTakeXp extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "takexp";
    }

    @Override
    public String getPermission()
    {
        return null;
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("trade XP for currency");
        if (actor.isAdmin()) {
            help.setArgs(
                new CommandHelpArgument("levels", "the number of XP levels to take", true),
                new CommandHelpArgument("player", "the name of the player to trade currency", false)
            );
        }
        else {
            help.setArgs(
                new CommandHelpArgument("levels", "the number of XP levels to take", true)
            );
        }
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return ((actor.getNumArgs() == 3 && actor.isAdmin()) || actor.getNumArgs() == 2);
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresPlayer(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        int levels = actor.getArgInt(1, String.format(Resources.INVALID_DECIMAL, "number of XP levels"));

        if (levels < 0) {
            actor.exitError(Resources.INVALID_DECIMAL, "number of XP levels");
        }

        Player p;
        if (actor.isAdmin() && actor.getNumArgs() == 3) {
            OfflinePlayer offlinePlayer = actor.getArgPlayer(2);
            if (!offlinePlayer.isOnline())
                actor.exitError(Resources.NOT_ONLINE);
            p = offlinePlayer.getPlayer();
        }
        else {
            p = actor.getPlayer();
        }

        double money = levels * ShopPlugin.getSavedState().getConfig().getXpConvert();

        if (levels > p.getLevel()) {
            actor.exitError("You do not have enough experience for this exchange.");
        } else {
            ShopPlugin.getEconomy().depositPlayer(p, money);
            p.setLevel(p.getLevel() - levels);

            p.sendMessage(String.format("You have exchanged %s levels for %s", Format.enchantments(levels + " XP"), Format.money(money)));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        ShopCmdActor actor = (ShopCmdActor)sender;
        if (actor.isAdmin() && actor.getNumArgs() == 3) {
            return ShopPlugin.getRegisteredPlayers().stream()
                .map(StoredPlayer::getName)
                .collect(Collectors.toList());
        }
        return super.onTabComplete(sender, command, alias, args);
    }
}
