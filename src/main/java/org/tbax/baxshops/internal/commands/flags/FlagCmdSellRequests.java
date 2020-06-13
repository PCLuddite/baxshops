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
import org.tbax.baxshops.commands.CmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.commands.ShopCmdActor;

public final class FlagCmdSellRequests extends FlagCmd
{
    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "sellrequest", "sell_request", "sell_requests" };
    }

    @Override
    public boolean requiresRealOwner(@NotNull CmdActor actor)
    {
        return true;
    }

    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "sellrequests";
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "require requests to sell to this shop");
        help.setLongDescription("Require players to send a request before selling an item. This way you can screen what you buy.");
        help.setArgs(
                new CommandHelpArgument("true|false", "whether or not this flag is enabled", true)
        );
        return help;
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        boolean value = actor.getArg(2).asBoolean("Usage:\n/shop flag sellrequests [true|false]");
        assert shop != null;
        shop.setFlagSellRequests(value);
        actor.sendMessage(Format.flag("Sell requests") + " for this shop are " + Format.keyword(value ? "enabled" : "disabled"));
    }
}