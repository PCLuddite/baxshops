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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.errors.PrematureAbortException;

import java.util.Collections;
import java.util.List;

public abstract class BaxShopCommand implements TabCompleter
{
    public abstract @NotNull String getName();
    public abstract String getPermission();

    public CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        return new CommandHelp(getName(), getAliases());
    }

    public abstract boolean hasValidArgCount(@NotNull ShopCmdActor actor);
    public abstract boolean requiresSelection(@NotNull ShopCmdActor actor);
    public abstract boolean requiresOwner(@NotNull ShopCmdActor actor);
    public abstract boolean requiresPlayer(@NotNull ShopCmdActor actor);
    public abstract boolean requiresItemInHand(@NotNull ShopCmdActor actor);
    public abstract void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException;

    public @NotNull String[] getAliases()
    {
        return new String[]{getName()};
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasPermission(@NotNull ShopCmdActor actor)
    {
        if (getPermission() == null)
            return true;
        return actor.hasPermission(getPermission());
    }

    public boolean useAlternative(ShopCmdActor actor)
    {
        return false;
    }
    public boolean allowsExclusion(ShopCmdActor actor) { return false; }

    public @NotNull Class<? extends BaxShopCommand> getAlternative()
    {
        return this.getClass();
    }

    public final boolean requiresAdmin()
    {
        return "shops.admin".equalsIgnoreCase(getPermission());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        return Collections.emptyList();
    }
}
