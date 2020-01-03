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

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.*;
import org.tbax.baxshops.commands.CmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.Resources;
import org.tbax.baxshops.internal.items.ItemUtil;

import java.util.Arrays;
import java.util.List;

public final class CmdRestock extends ShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "restock";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "r" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "restock the shop");
        help.setLongDescription("Restock a shop with the item held in the main hand, or any item in the player's inventory");
        help.setArgs(
                new CommandHelpArgument("quantity", "the amount to restock", false)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 2 || actor.getNumArgs() == 1;
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
    public boolean requiresPlayer(@NotNull CmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() < 2 || !BaxQuantity.isAny(actor.getArg(1));
    }

    @Override
    public boolean allowsExclusion(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 2 && BaxQuantity.isAny(actor.getArg(1));
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        if (actor.getShop().hasFlagInfinite()) {
            actor.exitError("This shop does not need to be restocked.");
        }

        if (actor.getItemInHand() != null && actor.getNumArgs() == 1) {
            actor.appendArg(actor.getItemInHand().getAmount()); // restock all in hand if nothing specified
        }

        ItemStack stack = actor.getItemInHand();
        BaxEntry entry = null;
        if (stack != null && (entry = actor.getShop().find(stack)) == null && requiresItemInHand(actor)) {
            actor.exitError(Resources.NOT_FOUND_SHOPITEM);
        }
        assert entry != null;

        BaxQuantity qty = actor.getArgPlayerQty(1);
        List<BaxEntry> taken = actor.takeArgFromInventory(1);

        if (requiresItemInHand(actor)) {
            BaxEntry takenItem = taken.get(0);
            entry.add(takenItem.getAmount());
            if (!(qty.isAll() || qty.isMost()) && takenItem.getAmount() < qty.getQuantity()) {
                actor.sendMessage("Could only restock with " + ChatColor.RED + "%d %s" + ChatColor.RESET + ". The shop now has %s.",
                        takenItem.getAmount(), ItemUtil.getName(takenItem), Format.number(entry.getAmount())
                );
            }
            else {
                actor.sendMessage("Restocked with %s in inventory. The shop now has %s.",
                        Format.itemName(takenItem.getAmount(), ItemUtil.getName(entry)), Format.number(entry.getAmount())
                );
            }
        }
        else {
            if (taken.isEmpty()) {
                actor.sendMessage("You did not have any items that could be restocked at this shop.");
            }
            else {
                for (BaxEntry takenEntry : taken) {
                    if (takenEntry.getAmount() > 0) {
                        BaxEntry shopEntry = actor.getShop().find(takenEntry);
                        assert shopEntry != null;
                        shopEntry.add(takenEntry.getAmount());
                        actor.sendMessage("Restocked %s.", Format.itemName(takenEntry.getAmount(), ItemUtil.getName(takenEntry)));
                    }
                }
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        CmdActor actor = (CmdActor)sender;
        if (actor.getNumArgs() == 2) {
            return Arrays.asList("all", "any", "most", "stack");
        }
        return super.onTabComplete(sender, command, alias, args);
    }
}