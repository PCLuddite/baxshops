/*
 * Copyright (C) 2013-2019 Timothy Baxendale
 * Portions derived from Shops Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus.
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
package tbax.baxshops.commands;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.errors.PrematureAbortException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    public String getPermission()
    {
        return null;
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
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        actor.getShop().sort(Comparator.comparing(BaxEntry::getName));
        actor.sendMessage("Shop inventory is now sorted alphabetically");
    }
}
