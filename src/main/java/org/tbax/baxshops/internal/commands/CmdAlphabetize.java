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

import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.commands.BaxShopCommand;
import org.tbax.baxshops.commands.ShopCmdActor;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.ShopPlugin;

import java.util.Comparator;

public final class CmdAlphabetize extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "alphabetize";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "alphabetize", "alphabetise" };
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "sorts shop inventory alphabetically");
        help.setLongDescription("Sort all inventory in the shop alphabetically");
        return help;
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
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
        return false;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor)
    {
        actor.getShop().sort(Comparator.comparing(BaxEntry::getName));
        ShopPlugin.sendMessage(actor, "Shop inventory is now sorted alphabetically");
    }
}