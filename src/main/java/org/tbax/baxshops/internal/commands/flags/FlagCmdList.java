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
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.commands.CmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.commands.ShopCmdActor;
import org.tbax.baxshops.internal.commands.ShopCmdArg;

import java.util.Collections;
import java.util.List;

public final class FlagCmdList extends FlagCmd
{
    @Override
    public @NotNull String getName()
    {
        return "list";
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "list all flags");
        help.setLongDescription("List all flags currently applied to this shop");
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 2;
    }

    @Override
    public boolean requiresRealOwner(@NotNull CmdActor actor)
    {
        return false;
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        if (!actor.isAdmin() && !shop.getOwner().equals(actor.getPlayer())) {
            actor.exitError("You do not have permission to view this shop's flags");
        }
        actor.getSender().sendMessage("\nFlags currently applied to this shop:");
        actor.getSender().sendMessage(String.format("%s: %s", Format.flag("Infinite"), Format.keyword(shop.hasFlagInfinite() ? "Yes" : "No")));
        actor.getSender().sendMessage(String.format("%s: %s", Format.flag("Sell to Shop"), Format.keyword(shop.hasFlagSellToShop() ? "Yes" : "No")));
        actor.getSender().sendMessage(String.format("%s: %s", Format.flag("Sell Requests"), Format.keyword(shop.hasFlagSellRequests() ? "Yes" : "No")));
        actor.getSender().sendMessage(String.format("%s: %s", Format.flag("Buy Requests"), Format.keyword(shop.hasFlagBuyRequests() ? "Yes" : "No")));
        if (actor.isAdmin()) {
            actor.getSender().sendMessage(String.format("%-20s: %s", Format.flag("Smart Stack"), Format.keyword(shop.hasFlagSmartStack() ? "Yes" : "No")));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull ShopCmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<ShopCmdArg> args)
    {
        return Collections.emptyList();
    }
}
