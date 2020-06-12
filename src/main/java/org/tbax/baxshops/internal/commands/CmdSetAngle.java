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

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.CommandHelpArgument;
import org.tbax.baxshops.commands.CmdActor;
import org.tbax.baxshops.commands.CommandArgument;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.items.ItemUtil;

import java.util.Arrays;
import java.util.List;

public final class CmdSetAngle extends ShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "setangle";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "setface", "face", "rotate" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "rotate the shop sign");
        help.setLongDescription("Rotate a shop to face a given direction");
        help.setArgs(
                new CommandHelpArgument("direction", "either NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST or NORTHWEST", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
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
        String direction = actor.getArg(1).asEnum(
                "NORTH", "NORTHEAST", "EAST", "SOUTHEAST", "SOUTH", "SOUTHWEST", "WEST", "NORTHWEST"
        );

        Block block = actor.getSelection().getLocation().getBlock();
        if (!ItemUtil.isSign(block.getType())) {
            actor.exitError("The location you selected is not a sign!");
        }

        if (ItemUtil.isWallSign(block)) {
            actor.exitError("You cannot rotate a wall sign. Only sign posts have a rotation.");
        }

        BlockFace face;
        try {
            face = BlockFace.valueOf(direction);
        }
        catch (IllegalArgumentException e) {
            face = BlockFace.valueOf(direction.substring(0, 5) + "_" + direction.substring(5));
        }

        BlockFace old = ItemUtil.getSignFacing(block);
        ItemUtil.setSignFacing(block, face);
        if (old == face) {
            actor.sendWarning("The shop is already facing " + direction);
        }
        else {
            actor.sendMessage("Rotated shop from facing " + old.name().replace("_", "") + " to " + direction);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<? extends CommandArgument> args)
    {
        return Arrays.asList(
                "NORTH", "NORTHEAST", "EAST", "SOUTHEAST", "SOUTH", "SOUTHWEST", "WEST", "NORTHWEST"
        );
    }
}
