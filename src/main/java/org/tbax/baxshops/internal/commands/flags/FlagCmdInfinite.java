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

import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.CommandHelpArgument;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.commands.ShopCmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;

public final class FlagCmdInfinite extends FlagCmd
{
    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "infinite", "isinfinite", "inf" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_ADMIN;
    }

    @Override
    public boolean requiresRealOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "allow this shop to have limitless inventory");
        help.setLongDescription("Allows you to create a shop that never runs out of inventory.");
        help.setArgs(
                new CommandHelpArgument("true|false", "whether or not this flag is enabled", true)
        );
        return help;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        boolean value = actor.getArgBoolean(2, "Usage:\n/shop flag infinite [true|false]");
        assert shop != null;
        shop.setFlagInfinite(value);
        actor.sendMessage(Format.flag("Infinite items") + " for this shop are " + Format.keyword(value ? "enabled" : "disabled"));
    }
}
