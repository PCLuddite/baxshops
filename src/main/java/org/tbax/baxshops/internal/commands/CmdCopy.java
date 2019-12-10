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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.commands.BaxShopCommand;
import org.tbax.baxshops.commands.ShopCmdActor;
import org.tbax.baxshops.PlayerUtil;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.items.ItemUtil;

public final class CmdCopy extends BaxShopCommand
{
    @Override
    public @NotNull  String getName()
    {
        return "copy";
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "copy a shop to your inventory");
        help.setLongDescription("Copies a selected shop using a sign from the player's inventory.");
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
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException // tested OK 3-14-19
    {
        Material signType;
        if (actor.isAdmin()) {
            if (ItemUtil.isSign(actor.getItemInHand())) {
                signType = actor.getItemInHand().getType();
            }
            else {
                signType = ItemUtil.getDefaultSignType();
            }
        }
        else {
            PlayerInventory inv = actor.getPlayer().getInventory();
            ItemStack sign = PlayerUtil.findSign(actor.getPlayer());
            if (sign == null) {
                actor.exitError("You need a sign to copy a shop.");
            }
            signType = sign.getType();
            inv.remove(new ItemStack(sign.getType(), 1));
        }

        int overflow = actor.giveItem(actor.getSelection().toItem(signType));
        if (overflow > 0) {
            actor.sendMessage("Your inventory is full");
            if (!actor.isAdmin()) {
                actor.getPlayer().getInventory().addItem(new ItemStack(signType, 1));
            }
        }
    }
}
