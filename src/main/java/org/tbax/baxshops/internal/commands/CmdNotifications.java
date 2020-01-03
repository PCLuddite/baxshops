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
package org.tbax.baxshops.internal.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.commands.CmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.ShopPlugin;

import java.util.Collections;
import java.util.List;

public final class CmdNotifications extends ShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "notifications";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "pending", "p", "n" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_TRADER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "show latest notification");
        help.setLongDescription(
                "Shows your latest notification and remove it from your notification queue. " +
                        "A notification can be an offer (e.g., someone wishes to sell you an item) " +
                        "or simply a message (e.g., an offer was accepted). " +
                        "Use /shop accept and /shop reject on offers."
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
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
    public boolean requiresPlayer(@NotNull CmdActor actor)
    {
        return !(actor.getNumArgs() == 2 && actor.getArg(1).equalsIgnoreCase("clear"));
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull CmdActor actor)
    {
        if (actor.getNumArgs() == 2 && actor.getArg(1).equalsIgnoreCase("clear"))
            return actor.isAdmin();
        return true;
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
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
        CmdActor actor = (CmdActor)sender;
        if (actor.isAdmin() && actor.getNumArgs() == 2) {
            return Collections.singletonList("clear");
        }
        return super.onTabComplete(sender, command, alias, args);
    }
}