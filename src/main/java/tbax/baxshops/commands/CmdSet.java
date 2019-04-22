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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.ItemNames;

import java.util.List;
import java.util.stream.Collectors;

public final class CmdSet extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "set";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"set","setprice"};
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
        help.setDescription("change the buy or sell price for a shop item");
        help.setArgs(
            new CommandHelpArgument("item", "the item in the shop", true),
            new CommandHelpArgument("$buy", "the new price for buying a single item", true),
            new CommandHelpArgument("$sell", "the new price for selling a single item. If no price is specified, the item cannot be sold.", false)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 3 || actor.getNumArgs() == 4;
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
        if (actor.getNumArgs() == 3) {
            actor.appendArg(-1);
        }

        BaxEntry entry = actor.getArgEntry(1);

        double retailAmount = actor.getArgRoundedDouble(2, String.format(Resources.INVALID_DECIMAL, "buy price")),
                refundAmount = actor.getArgRoundedDouble(3, String.format(Resources.INVALID_DECIMAL, "sell price"));

        entry.setRetailPrice(retailAmount);
        entry.setRefundPrice(refundAmount);

        if (shop.hasFlagInfinite()) {
            actor.sendMessage("The price for %s was set.", Format.itemName(ItemNames.getName(entry)));
        }
        else {
            actor.sendMessage("The price for %s was set.", Format.itemName(entry.getAmount(), ItemNames.getName(entry)));
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
