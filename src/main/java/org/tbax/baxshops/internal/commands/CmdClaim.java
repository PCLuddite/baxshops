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
package org.tbax.baxshops.internal.commands;

import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.commands.CmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.Resources;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.notification.Claimable;
import org.tbax.baxshops.notification.Notification;
import org.tbax.baxshops.serialization.StoredPlayer;

public final class CmdClaim extends ShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "claim";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "c" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_TRADER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "claim your most recent notification");
        help.setLongDescription("If your most recent notification is a claim, the item will be added to your inventory " +
                "and this notification removed from the queue. You will not be able to claim an item if there is not enough room in your inventory.");
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresPlayer(@NotNull CmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException // tested OK 4-21-19
    {
        StoredPlayer player = actor.getStoredPlayer();
        if (!player.hasNotes()) {
            actor.exitError(Resources.NOT_FOUND_NOTE);
        }
        else {
            Notification n = player.peekNote();
            if (n instanceof Claimable) {
                Claimable c = (Claimable)n;
                if (c.claim(actor)) {
                    player.dequeueNote();
                }
                ShopPlugin.showNotificationCount(actor.getPlayer());
            }
            else {
                actor.sendError("Your current notification is not a claim");
            }
        }
    }
}