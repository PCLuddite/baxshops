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
package org.tbax.baxshops.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.tbax.bukkit.commands.BaxCommand;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.errors.PrematureAbortException;
import org.tbax.baxshops.Permissions;

import java.util.List;

public abstract class ShopCommand extends BaxCommand
{
    @Override
    public String getCommand()
    {
        return "shop";
    }

    public abstract boolean requiresSelection(@NotNull ShopCmdActor actor);
    public abstract boolean requiresOwner(@NotNull ShopCmdActor actor);
    public abstract boolean requiresItemInHand(@NotNull ShopCmdActor actor);

    public boolean allowsExclusion(ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresAdmin()
    {
        return Permissions.SHOP_ADMIN.equalsIgnoreCase(getPermission());
    }

    @Override
    public void onCommand(@NotNull CmdActor actor) throws PrematureAbortException
    {
        if (actor instanceof ShopCmdActor) {
            onShopCommand((ShopCmdActor)actor);
        }
    }

    public abstract void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException;

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, String[] args)
    {
        return onTabComplete((ShopCmdActor)sender, command, alias,((ShopCmdActor)sender).getArgs());
    }
}
