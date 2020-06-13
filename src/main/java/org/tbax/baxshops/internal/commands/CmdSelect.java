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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.CommandHelpArgument;
import org.tbax.baxshops.commands.CmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.internal.ShopSelection;
import org.tbax.baxshops.serialization.StoredPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CmdSelect extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "select";
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_ADMIN;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "change your current shop selection");
        help.setLongDescription("Select a shop based on its UUID. If the shop has multiple locations " +
                "and no location is specified, the first shop is selected.");
        help.setArgs(
                new CommandHelpArgument("uuid", "the unique ID of the shop", true),
                new CommandHelpArgument("location", "the shop location. See /shop list", 1)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 2 || actor.getNumArgs() == 3;
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
        return false;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        if (actor.getNumArgs() == 2)
            actor.appendArg("1");

        UUID shopId = actor.getArg(1).asUuid();
        BaxShop shop = ShopPlugin.getShop(shopId);
        if (shop == null)
            actor.sendError("The given id doesn't match any existing shop");

        List<Location> locations = new ArrayList<>(shop.getLocations());
        int locNbr = actor.getArg(2).asInteger();
        if (locNbr < 1 || locNbr > locations.size()) {
            actor.exitError("That's not a valid location");
        }

        OfflinePlayer player;
        if (actor.getSender() instanceof Player) {
            player = actor.getPlayer();
        }
        else {
            player = StoredPlayer.DUMMY;
        }

        ShopSelection selection = ShopPlugin.getSelection(player);
        selection.setLocation(locations.get(locNbr - 1));
        selection.setShop(shop);
        selection.setIsOwner(shop.getOwner().equals(StoredPlayer.DUMMY));
        selection.setPage(0);

        selection.showIntro(actor.getSender());
        selection.showListing(actor.getSender());
    }
}