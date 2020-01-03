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

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.CommandHelpArgument;
import org.tbax.baxshops.commands.BaxShopCommand;
import org.tbax.baxshops.commands.ShopCmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.internal.ShopSelection;
import org.tbax.baxshops.serialization.StoredPlayer;

public final class CmdPage extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "page";
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_ADMIN;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "set the page of the current selection");
        help.setLongDescription("Sets the page of the player's current shop selection");
        help.setArgs(
                new CommandHelpArgument("page", "the page number to display", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
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
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        int page = actor.getArgInt(1);
        if (page < 1) {
            page = actor.getShop().getPages();
        }
        else if (page > actor.getShop().getPages()) {
            page = 1;
        }

        OfflinePlayer player;
        if (actor.getSender() instanceof Player) {
            player = actor.getPlayer();
        }
        else {
            player = StoredPlayer.DUMMY;
        }

        ShopSelection selection = ShopPlugin.getSelection(player);
        selection.setPage(page - 1);
        selection.showListing(actor.getSender());
    }
}