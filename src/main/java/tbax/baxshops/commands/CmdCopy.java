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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.errors.PrematureAbortException;

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
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("Copies the shop using a sign from the player's inventory");
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
        if (!actor.isAdmin()) {
            PlayerInventory inv = actor.getPlayer().getInventory();
            ItemStack sign = new ItemStack(Material.SIGN, 1);
            if (!inv.containsAtLeast(sign, 1)) {
                actor.exitError("You need a sign to copy a shop.");
            }
            inv.removeItem(sign);
        }

        int i = actor.giveItem(actor.getSelection().toItem());
        if (i > 0) {
            actor.sendMessage("Your inventory is full");
            if (!actor.isAdmin()) {
                actor.getPlayer().getInventory().addItem(new ItemStack(Material.SIGN, 1));
            }
        }
    }
}
