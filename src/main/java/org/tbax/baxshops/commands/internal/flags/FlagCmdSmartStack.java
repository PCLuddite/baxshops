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
package org.tbax.baxshops.commands.internal.flags;

import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.CommandHelpArgument;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.commands.ShopCmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;

public final class FlagCmdSmartStack extends FlagCmd
{
    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "smartstack", "smart_stack" };
    }

    @Override
    public boolean requiresRealOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public String getPermission()
    {
        return "shops.admin";
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "enable certain like-items to be stacked");
        help.setLongDescription("Enables you to stack certain items that are essentially equivalent, " +
                        "ignoring certain meta data (e.g. enchanted books, banners, etc). " +
                        "This flag is still in beta and can only be used by an admin.");
        help.setArgs(
                new CommandHelpArgument("true|false", "whether or not this flag is enabled", true)
        );
        return help;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        boolean value = actor.getArgBoolean(2, "Usage:\n/shop flag smartstack [true|false]");
        assert shop != null;
        shop.setFlagSmartStack(value);
        actor.sendMessage(Format.flag("Smart stacking") + " for this shop is " + Format.keyword(value ? "enabled" : "disabled"));
    }
}
