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
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.CommandHelpArgument;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.commands.CmdActor;
import org.tbax.baxshops.commands.CommandArgument;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.Resources;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.serialization.StoredPlayer;

import java.util.List;
import java.util.stream.Collectors;

public final class CmdSetOwner extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "setowner";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "owner" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "set the owner of a shop");
        help.setLongDescription("Transfers ownership of a shop to another player. When changing the owner from yourself," +
                        " you will still have control of the shop until you walk away.");
        help.setArgs(
                new CommandHelpArgument("player", "the name or UUID of the new owner", true)
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
        return true;
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
        BaxShop shop = actor.getShop();
        StoredPlayer newOwner = actor.getArg(1).asPlayer();
        if (newOwner == null) {
            if (actor.isAdmin()) { // only admin can set owner to non-registered player
                newOwner = actor.getArg(1).asPlayerSafe();
            }
            else {
                actor.exitError(Resources.NOT_REGISTERED_PLAYER, actor.getArg(2), "be a shop owner");
            }
        }
        shop.setOwner(newOwner);
        actor.sendMessage(Format.username(shop.getOwner().getName()) + " is now the owner!");
        if (actor.isOwner()) {
            actor.sendMessage("You will still be able to edit this shop until you leave or reselect it.");
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<? extends CommandArgument> args)
    {
        return ShopPlugin.getRegisteredPlayers().stream()
                .map(StoredPlayer::getName)
                .collect(Collectors.toList());
    }
}
