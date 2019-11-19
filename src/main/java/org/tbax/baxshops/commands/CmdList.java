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
package org.tbax.baxshops.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.ShopSelection;
import org.tbax.baxshops.CommandHelp;

public final class CmdList extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "list";
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("List all locations for this shop");
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresPlayer(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor)
    {
        ShopSelection selection = actor.getSelection();
        actor.sendMessage(Format.header("Shop Locations"));
        if (!selection.getShop().getLocations().isEmpty()) {
            int index = 0;
            actor.sendMessage(" %-3s %-16s %-18s", ChatColor.GRAY + "#", ChatColor.WHITE + "Location", ChatColor.WHITE + "Sign Text");
            for(Location loc : selection.getShop().getLocations()) {
                actor.sendMessage("%-3s %-16s %-18s %s",
                                ChatColor.WHITE.toString() + ++index + ".",
                                Format.location(loc),
                                ChatColor.LIGHT_PURPLE + selection.getShop().getSignTextString(loc),
                                (selection.getLocation().equals(loc) ? ChatColor.LIGHT_PURPLE + " (current)" : ""));
            }
        }
        else {
            actor.sendMessage(ChatColor.YELLOW + "This shop has no other locations.");
        }
    }
}
