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
package org.tbax.baxshops.internal.commands;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.CommandHelpArgument;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.commands.BaxShopCommand;
import org.tbax.baxshops.commands.ShopCmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.Resources;
import org.tbax.baxshops.internal.items.ItemUtil;

public final class CmdAdd extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "add";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "add", "+", "ad" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "add an item to a shop");
        help.setLongDescription("Adds the item in the main hand to the selected shop");
        help.setArgs(
                new CommandHelpArgument("buy price", "the retail price for a single item in the stack", true),
                new CommandHelpArgument("sell price", "the refund price of a single item in the stack (if left blank the item cannot be sold to the shop)", false)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 2 || actor.getNumArgs() == 3;
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
        return true;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException // tested OK 3-13-19
    {
        BaxEntry newEntry = new BaxEntry();
        double retailAmount = actor.getArgRoundedDouble(1, String.format(Resources.INVALID_DECIMAL, "buy price")),
                refundAmount = 0d;

        if (actor.getNumArgs() == 3) {
            refundAmount = actor.getArgRoundedDouble(2, String.format(Resources.INVALID_DECIMAL, "sell price"));
            newEntry.canSell(true);
        }
        else {
            newEntry.canSell(false);
        }

        ItemStack stack = actor.getItemInHand();
        assert stack != null;
        assert actor.getShop() != null;
        if (ItemUtil.isShop(stack)) {
            actor.exitError("You can't add a shop to a shop.");
        }
        if (actor.getShop().contains(stack)) {
            actor.exitError("That item has already been added to this shop\n" +
                    "Use /shop restock to restock"
            );
        }

        newEntry.canBuy(true);
        newEntry.setItem(stack, stack.getAmount());
        newEntry.setRetailPrice(retailAmount);
        newEntry.setRefundPrice(refundAmount);
        actor.getShop().add(newEntry);
        actor.sendMessage("A new entry for %s was added to the shop.", Format.itemName(newEntry.getAmount(), ItemUtil.getName(newEntry)));
        if (!actor.getShop().hasFlagInfinite()) {
            actor.setItemInHand(null);
        }
    }
}