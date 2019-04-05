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

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.CommandHelpArgument;
import tbax.baxshops.Format;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;

public final class CmdSetAngle extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "setangle";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"setangle","setface","face"};
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
        help.setDescription("rotates a shop sign to face another direction");
        help.setArgs(
            new CommandHelpArgument("direction", "A cardinal direction (i.e., 'north', 'south', 'east' or 'west')", true)
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
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        Block b = actor.getSelection().getLocation().getBlock();

        byte angle = 0;
        try {
            angle = (byte)((Integer.parseInt(actor.getArg(1)) % 4) << 2);
        }
        catch(NumberFormatException e) {
            switch(actor.getArg(1).toLowerCase()) {
                case "south": angle = 0; break;
                case "west": angle = 1; break;
                case "north": angle = 2; break;
                case "east": angle = 3; break;
                default:
                    actor.exitError("The direction you entered wasn't valid! Use one of the four cardinal directions.");
            }
            angle = (byte)(angle << 2);
        }
        try {
            b.setData(angle, false);
            actor.sendMessage("Sign rotated to face " + Format.keyword(actor.getArg(1).toLowerCase()));
        }
        catch(Exception e) {
            actor.exitError("An error occurred when trying to rotate the sign. The sign may not have been rotated");
        }
    }
}
