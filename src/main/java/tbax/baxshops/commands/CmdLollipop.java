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
package tbax.baxshops.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.CommandHelpArgument;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.notification.LollipopNotification;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdLollipop extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "lollipop";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"lollipop","lol","lolly"};
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
        help.setDescription("Hand out lollipop");
        help.setArgs(
            new CommandHelpArgument("player", "player to send lollipop", true),
            new CommandHelpArgument("tastiness", "the tastiness (0-100)", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 3 || actor.getNumArgs() == 2;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
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
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException // tested OK 3-14-19
    {
        double tastiness = LollipopNotification.DEFAULT_TASTINESS;
        if (actor.getNumArgs() == 3) {
            tastiness = actor.getArgDouble(2, "Invalid tastiness");
        }
        LollipopNotification lol = new LollipopNotification(actor.getPlayer(), tastiness);
        OfflinePlayer recipient = actor.getArgPlayer(1);
        ShopPlugin.sendNotification(recipient, lol);
        actor.sendMessage("Sent a %s lollipop to %s", lol.getTastiness(), recipient.getName());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        ShopCmdActor actor = (ShopCmdActor)sender;
        if (actor.getNumArgs() == 2) {
            return ShopPlugin.getRegisteredPlayers().stream()
                .map(StoredPlayer::getName)
                .collect(Collectors.toList());
        }
        else if (actor.getNumArgs() == 3) {
            return Arrays.asList("0", "10", "20", "30", "40", "50", "55", "60", "70", "80", "90", "100");
        }
        else {
            return super.onTabComplete(sender, command, alias, args);
        }
    }
}
