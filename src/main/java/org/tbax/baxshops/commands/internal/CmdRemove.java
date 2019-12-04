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
package org.tbax.baxshops.commands.internal;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.*;
import org.tbax.baxshops.commands.BaxShopCommand;
import org.tbax.baxshops.commands.ShopCmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Resources;
import org.tbax.baxshops.internal.items.ItemUtil;

import java.util.List;

public final class CmdRemove extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "remove";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"remove","rm"};
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "remove an item from the shop");
        help.setLongDescription("Remove an entry from a shop. The current stock will be added to your inventory.");
        help.setArgs(
            new CommandHelpArgument("entry", "the name or entry number of the item to remove", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 2;
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
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException // tested OK 3/30/19
    {
        BaxShop shop = actor.getShop();
        BaxEntry entry = actor.getArgEntry(1);

        if (entry == null) {
            actor.exitError(Resources.NOT_FOUND_SHOPITEM);
        }
        assert shop != null;
        if (!shop.hasFlagInfinite() && entry.getAmount() > 0) {
            ItemStack stack = entry.toItemStack();
            int overflow = actor.giveItem(stack, false);
            entry.subtract(stack.getAmount() - overflow);
            if (overflow > 0) {
                actor.sendMessage(Resources.SOME_ROOM, stack.getAmount() - overflow, entry.getName());
            }
            else {
                actor.sendMessage("%s %s added to your inventory.",
                    Format.itemName(stack.getAmount(), ItemUtil.getName(entry)),
                    stack.getAmount() == 1 ? "was" : "were");
            }
        }
        if (!shop.hasFlagInfinite() && entry.getAmount() > 0) {
            actor.sendWarning("The shop entry was not removed");
        }
        else {
            shop.remove(entry);
            actor.sendMessage("The shop entry was removed.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        ShopCmdActor actor = (ShopCmdActor)sender;
        if (args.length == 2 && actor.getShop() != null) {
            return actor.getShop().getAllItemAliases();
        }
        else {
            return super.onTabComplete(sender, command, alias, args);
        }
    }
}
