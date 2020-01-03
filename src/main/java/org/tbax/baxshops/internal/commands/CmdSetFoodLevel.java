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

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.CommandHelpArgument;
import org.tbax.baxshops.commands.CmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;

public final class CmdSetFoodLevel extends ShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "sethunger";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "sethungerlevel", "setfoodlevel", "setfood" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_ADMIN;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "set hunger level");
        help.setLongDescription("Sets the hunger level of a player");
        help.setArgs(
                new CommandHelpArgument("level", "The hunger level out of 20", true),
                new CommandHelpArgument("player", "the players whose hunger to change", (actor.getSender() == Bukkit.getConsoleSender()))
        );
        if (!help.getArgs()[1].isRequired()) {
            help.getArgs()[1].setDefaultValue(actor.getSender().getName());
        }
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 3 || (actor.getNumArgs() == 2 && actor.getSender() != Bukkit.getConsoleSender());
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
        int level = actor.getArgInt(1);
        OfflinePlayer player;
        if (actor.getNumArgs() == 2) {
            player = actor.getPlayer();
        }
        else {
            player = actor.getArgPlayer(1);
            if (!player.isOnline()) {
                actor.exitError("The player must be online to change their hunger level");
            }
        }
        player.getPlayer().setFoodLevel(level);
    }
}