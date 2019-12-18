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
package org.tbax.baxshops.internal.commands.flags;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.CommandHelpArgument;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.commands.ShopCmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Resources;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.serialization.StoredPlayer;

import java.util.List;
import java.util.stream.Collectors;

public final class FlagCmdOwner extends FlagCmd
{
    @Override
    public boolean requiresRealOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public @NotNull String getName()
    {
        return "owner";
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "set the owner of a shop");
        help.setLongDescription("Transfers ownership of a shop to another player");
        help.setArgs(
                new CommandHelpArgument("new owner", "the name or UUID of the new owner", true)
        );
        return help;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        assert shop != null;
        StoredPlayer newOwner = actor.getArgPlayer(2);
        if (newOwner == null) {
            if (actor.isAdmin()) { // only admin can set owner to non-registered player
                newOwner = actor.getArgPlayerSafe(2);
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
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        return ShopPlugin.getRegisteredPlayers().stream()
                .map(StoredPlayer::getName)
                .collect(Collectors.toList());
    }
}