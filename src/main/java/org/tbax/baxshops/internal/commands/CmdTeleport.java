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

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.CommandHelpArgument;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.commands.CmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.ShopSelection;

import java.util.Arrays;
import java.util.List;

public final class CmdTeleport extends ShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "teleport";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "tp" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_ADMIN;
    }

    @Override
    public CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "teleport to a shop location");
        help.setLongDescription("Teleport to a specific shop location. Use /shop list for a list of locations. This can only be done by an admin.");
        help.setArgs(
                new CommandHelpArgument("index", "the index of the shop location", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 2;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresPlayer(@NotNull CmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        ShopSelection selection = actor.getSelection();

        int loc = actor.getArgInt(1, "Expected a location number. For a list of locations, use /shop list.");
        if (loc < 1 || loc > selection.getShop().getLocations().size()) {
            actor.exitError("That shop location does not exist.");
        }

        Location old = selection.getLocation();
        selection.setLocation((Location)selection.getShop().getLocations().toArray()[loc - 1]);
        if (actor.getPlayer().teleport(selection.getLocation())) {
            actor.sendMessage("Teleported you to %s", Format.location(selection.getLocation()));
        }
        else {
            selection.setLocation(old);
            actor.exitError("Unable to teleport you to that location.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        ShopCmdActor actor = (ShopCmdActor)sender;
        if (actor.isAdmin() && actor.getNumArgs() == 2 && actor.getShop() != null) {
            String[] nums = new String[actor.getShop().getLocations().size()];
            for (int i = 0; i < nums.length; ++i) {
                nums[i] = String.valueOf(i + 1);
            }
            return Arrays.asList(nums);
        }
        return super.onTabComplete(sender, command, alias, args);
    }
}