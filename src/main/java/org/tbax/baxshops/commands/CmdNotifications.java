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
package org.tbax.baxshops.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.errors.PrematureAbortException;

import java.util.Collections;
import java.util.List;

public final class CmdNotifications extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "notifications";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"notifications","pending","p","n"};
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
        help.setDescription(
            "Shows a list of notifications to sell items to your shops\n" +
            "These can be offers (e.g., someone wishes to sell you an item)\n" +
            "or messages (e.g., an offer was accepted).\n" +
            "Use /shop accept and /shop reject on offers."
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return (actor.isAdmin() && actor.getNumArgs() == 2) || actor.getNumArgs() == 1;
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
        return !(actor.getNumArgs() == 2 && actor.getArg(1).equalsIgnoreCase("clear"));
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull ShopCmdActor actor) {
        if (actor.getNumArgs() == 2 && actor.getArg(1).equalsIgnoreCase("clear"))
            return actor.isAdmin();
        return true;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        if (actor.getNumArgs() == 1) {
            if (ShopPlugin.showNotificationCount(actor.getPlayer()) > 0) {
                ShopPlugin.showNotification(actor.getPlayer());
            }
        }
        else if (actor.getNumArgs() == 2) {
            if (actor.getArg(1).equalsIgnoreCase("clear")) {
                actor.getStoredPlayer().clearNotes();
                actor.getPlayer().sendMessage("Your notifications have been cleared");
            }
            else {
                actor.exitError("Unknown notification action %s", actor.getArg(2));
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        ShopCmdActor actor = (ShopCmdActor)sender;
        if (actor.isAdmin() && actor.getNumArgs() == 2) {
            return Collections.singletonList("clear");
        }
        return super.onTabComplete(sender, command, alias, args);
    }
}
