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
package org.tbax.baxshops.internal.notification;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.notification.Claimable;
import org.tbax.baxshops.notification.StandardNote;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class SaleClaim extends StandardNote implements Claimable
{
    public SaleClaim(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public SaleClaim(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public SaleClaim(Map<String, Object> args)
    {
        super(args);
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getBuyer().equals(sender)) {
            return String.format("You bought %s from %s for %s",
                entry.getFormattedName(), Format.username(buyer), entry.getFormattedSellPrice()
            );
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s has sold %s to %s for %s",
            Format.username(seller), entry.getFormattedName(), Format.username2(buyer), entry.getFormattedSellPrice()
        );
    }

    @Override
    public @NotNull UUID getRecipientId()
    {
        return buyer;
    }

    @Override
    public void setRecipient(@NotNull OfflinePlayer player)
    {
        buyer = player.getUniqueId();
    }

    public static SaleClaim deserialize(Map<String, Object> args)
    {
        return new SaleClaim(args);
    }

    public static SaleClaim valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
