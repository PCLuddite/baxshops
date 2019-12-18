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
package org.tbax.baxshops.internal.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.CommandHelpArgument;
import org.tbax.baxshops.commands.BaxShopCommand;
import org.tbax.baxshops.commands.ShopCmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.Resources;

import java.util.List;

public final class CmdSetIndex extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "setindex";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "setindex", "setorder", "reorder" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "change entry order");
        help.setLongDescription("Change the order of an entry in the shop");
        help.setArgs(
                new CommandHelpArgument("old-index", "the current index of the item", true),
                new CommandHelpArgument("new-index", "the new index of the item", true)
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
        return true;
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
        assert actor.getShop() != null;
        int oldIndex = actor.getShop().indexOf(actor.getArgEntry(1));
        int newIndex = actor.getArgInt(2, String.format(Resources.INVALID_DECIMAL, "new index"));
        if (newIndex > actor.getShop().size()) {
            actor.exitError("You must choose a new index that is less than the number of items in the shop!");
        }
        if (newIndex < 1) {
            actor.exitError("The new index must be greater than 0.");
        }
        if (newIndex == oldIndex) {
            actor.exitWarning("The index has not been changed.");
        }
        BaxEntry entry = actor.getShop().removeEntryAt(oldIndex);
        if (actor.getShop().size() < newIndex) {
            actor.getShop().add(entry);
        }
        else {
            actor.getShop().addEntry(newIndex - 1, entry);
        }
        actor.sendMessage("The index for this item was successfully changed.");
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