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
package org.tbax.baxshops.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.*;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.items.ItemUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CmdEmpty extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "empty";
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "remove all shop inventory");
        help.setLongDescription("Takes out all shop inventory and adds it to yours");
        help.setArgs(
                new CommandHelpArgument("entry", "the entry to start removing items at", 1)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 1 || actor.getNumArgs() == 2;
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
        int startIdx = 0;

        if (actor.getNumArgs() == 2)
            startIdx = actor.getArgEntryIndex(1);

        if (shop.isEmpty())
            actor.exitError("This shop has no inventory");

        if (shop.hasFlagInfinite())
            actor.exitError("You cannot empty the inventory of an infinite shop");

        for(int idx = startIdx; idx < shop.size(); ++idx) {
            BaxEntry entry = shop.getEntry(idx);
            ItemStack stack = entry.toItemStack();

            if (stack.getAmount() <= 0)
                continue;

            int overflow = PlayerUtil.giveItem(actor.getPlayer(), stack, false);
            entry.subtract(stack.getAmount() - overflow);
            if (overflow > 0) {
                if (stack.getAmount() > overflow) {
                    actor.exitMessage(Resources.SOME_ROOM, stack.getAmount() - overflow, ItemUtil.getName(stack));
                }
                else {
                    actor.exitError(Resources.NO_ROOM_FOR_ITEM, stack.getAmount(), ItemUtil.getName(stack));
                }
            }
            else {
                actor.sendMessage("%s was added to your inventory",
                    Format.itemName(stack.getAmount(), entry.getName()));
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        ShopCmdActor actor = (ShopCmdActor)sender;
        if (actor.getShop() != null && args.length == 2) {
            return IntStream.range(1, actor.getShop().size() + 1)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
        }
        return super.onTabComplete(sender, command, alias, args);
    }
}
