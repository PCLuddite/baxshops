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
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.CommandHelpArgument;
import org.tbax.baxshops.commands.CmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.Resources;

import java.util.List;

public final class CmdSetDur extends ShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "setdamage";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "setdmg", "setdurability", "setdur" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_ADMIN;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "set an item's damage percent");
        help.setLongDescription("Set the damage percentage for an item. This can only be done by an admin.");
        help.setArgs(
                new CommandHelpArgument("entry", "the item for which to set the durability", true),
                new CommandHelpArgument("damage", "the damage percentage", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
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
    public boolean requiresPlayer(@NotNull CmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxEntry entry = actor.getArg(1).asEntry();
        short damage = actor.getArg(2).asShort( String.format(Resources.INVALID_DECIMAL, "damage"));
        if (damage > 100 || damage < 0) {
            actor.exitError("Damage percentage must be between 0 and 100");
        }
        entry.setDamagePercent(damage);
        actor.sendMessage("The damage has been set.");
    }

    @Override
    public List<String> onTabComplete(@NotNull ShopCmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<ShopCmdArg> args)
    {
        if (args.size() == 2 && actor.getShop() != null) {
            return actor.getShop().getAllItemAliases();
        }
        else {
            return super.onTabComplete(actor, command, alias, args);
        }
    }
}