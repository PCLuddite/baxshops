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

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.ItemNames;

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
        return new String[]{"add","+","ad"};
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("add held item to this shop");
        help.setArgs(
          new CommandHelpArgument("buy price", "the price of a single item in the stack", true),
          new CommandHelpArgument("sell price", "the selling price of a single item in the stack (by default the item cannot be sold)", false)
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
        double retailAmount = actor.getArgRoundedDouble(1, String.format(Resources.INVALID_DECIMAL, "buy price")),
                refundAmount = -1;

        if (actor.getNumArgs() == 3) {
            refundAmount = actor.getArgRoundedDouble(2, String.format(Resources.INVALID_DECIMAL, "sell price"));
        }

        ItemStack stack = actor.getItemInHand();
        assert stack != null;
        assert actor.getShop() != null;
        if (BaxShop.isShop(stack)) {
            actor.exitError("You can't add a shop to a shop.");
        }
        if (actor.getShop().contains(stack)) {
            actor.exitError("That item has already been added to this shop\n" +
                            "Use /shop restock to restock"
            );
        }

        BaxEntry newEntry = new BaxEntry();
        newEntry.setItem(stack, stack.getAmount());
        newEntry.setRetailPrice(retailAmount);
        newEntry.setRefundPrice(refundAmount);
        actor.getShop().add(newEntry);
        actor.sendMessage("A new entry for %s was added to the shop.", Format.itemName(newEntry.getAmount(), ItemNames.getName(newEntry)));
        if (!actor.getShop().hasFlagInfinite()) {
            actor.getPlayer().getInventory().setItemInMainHand(null);
        }
    }
}
