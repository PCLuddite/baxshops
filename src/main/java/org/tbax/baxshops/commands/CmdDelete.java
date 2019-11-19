/*
 * Copyright (C) Timothy Baxendale
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
package org.tbax.baxshops.commands;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.errors.PrematureAbortException;

public final class CmdDelete extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "delete";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"delete","del"};
    }
    
    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "delete a shop");
        help.setLongDescription("Deletes the selected shop and turns it into a normal sign. Only owners and ops may do this. " +
                "If the shop has multiple locations, only that location will be deleted. If this is the only location of the shop, " +
                "all shop inventory must be removed prior to it being deleted. Infinite shops may be deleted without inventory being removed.");
        return help;
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
        BaxShop shop = actor.getShop();
        assert shop != null;
        if (shop.getLocations().size() == 1) {
            if (!shop.isEmpty() && !actor.getShop().hasFlagInfinite()) {
                actor.sendError("There is still inventory at this shop!");
                actor.sendError("Please remove all inventory before deleting it.");
            }
            else {
                removeShop(actor, actor.getShop());
                actor.sendMessage("%s's shop has been deleted", Format.username(shop.getOwner().getName()));
            }
        }
        else {
            ShopPlugin.removeLocation(shop.getId(), actor.getSelection().getLocation());
            changeSignText(actor, actor.getSelection().getLocation());
            actor.sendMessage("This shop location has been closed");
        }
        ShopPlugin.clearSelection(actor.getPlayer());
    }

    private void removeShop(ShopCmdActor actor, BaxShop shop) throws PrematureAbortException
    {
        for(Location loc : shop.getLocations()) {
            changeSignText(actor, loc);
            ShopPlugin.removeLocation(shop.getId(), loc);
        }
        ShopPlugin.removeShop(shop.getId());
    }

    private void changeSignText(ShopCmdActor actor, Location loc)
    {
        BaxShop shop = actor.getShop();
        assert shop != null;
        try {
            Block b = loc.getBlock();
            Sign sign = (Sign) b.getState();
            sign.setLine(0, "This shop has");
            sign.setLine(1, "been closed by");
            sign.setLine(2, (shop.getOwner().equals(actor.getPlayer()) ? "the owner" : "an admin") + ".");
            sign.setLine(3, "");
            sign.update();
        }
        catch (NullPointerException | ClassCastException e) {
            actor.sendError("Unable to change the sign text at " + Format.location(loc));
        }
    }
}
