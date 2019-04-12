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
package tbax.baxshops.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;

import java.util.List;
import java.util.stream.Collectors;

public final class CmdSetAmnt extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "setamnt";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"setamnt","setamt"};
    }

    @Override
    public String getPermission()
    {
        return "shops.admin";
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("set the quantity for an item in a shop");
        help.setArgs(
            new CommandHelpArgument("item", "the item for which to set the quantity", true),
            new CommandHelpArgument("quantity", "the quantity for the item", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 3;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresPlayer(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxEntry entry = actor.getArgEntry(1);

        int amnt = actor.getArgInt(2, String.format(Resources.INVALID_DECIMAL, "amount"));
        entry.setAmount(amnt);

        actor.sendMessage("The amount has been set.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        ShopCmdActor actor = (ShopCmdActor)sender;
        if (args.length == 2 && actor.getShop() != null) {
            return actor.getShop().getAllItemAliases().stream()
                .filter(n -> n.startsWith(actor.getArg(1).toLowerCase()))
                .collect(Collectors.toList());
        }
        else {
            return super.onTabComplete(sender, command, alias, args);
        }
    }
}
