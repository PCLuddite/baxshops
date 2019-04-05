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
package tbax.baxshops.commands.flags;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.errors.PrematureAbortException;

public final class FlagCmdList extends FlagCmd
{
    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"list"};
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 2;
    }

    @Override
    public boolean requiresRealOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        assert shop != null;
        actor.sendMessage("\nFlags currently applied to this shop:");
        actor.sendMessage("%s: %s", Format.flag("Infinite"), Format.keyword(shop.hasFlagInfinite() ? "Yes" : "No"));
        actor.sendMessage("%s: %s", Format.flag("Notify"), Format.keyword(shop.hasFlagNotify() ? "Yes" : "No"));
        actor.sendMessage("%s: %s", Format.flag("Sell to Shop"), Format.keyword(shop.hasFlagSellToShop() ? "Yes" : "No"));
        actor.sendMessage("%s: %s", Format.flag("Sell Requests"), Format.keyword(shop.hasFlagSellRequests() ? "Yes" : "No"));
        actor.sendMessage("%s: %s", Format.flag("Buy Requests"), Format.keyword(shop.hasFlagBuyRequests() ? "Yes" : "No"));
    }
}
