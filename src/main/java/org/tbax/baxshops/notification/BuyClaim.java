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
package org.tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.serialization.SafeMap;
import org.tbax.baxshops.serialization.states.State_00300;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class BuyClaim extends StandardNote implements Claimable
{
    public BuyClaim(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public BuyClaim(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public BuyClaim(Map<String, Object> args)
    {
        super(args);
    }

    @Override
    public void upgrade00300(@NotNull SafeMap map)
    {
        buyer = State_00300.getPlayerId(map.getString("buyer"));
        seller = State_00300.getPlayerId(map.getString("seller"));
        shopId = BaxShop.DUMMY_UUID;
        entry = map.getBaxEntry("entry");
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getBuyer().equals(sender)) {
            return String.format("%s accepted your request to buy %s for %s.",
                Format.username(seller),
                entry.getFormattedName(),
                entry.getFormattedBuyPrice()
            );
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s accepted %s's request to buy %s for %s.",
            Format.username(seller),
            Format.username2(buyer),
            entry.getFormattedName(),
            entry.getFormattedBuyPrice()
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

    public static BuyClaim deserialize(Map<String, Object> args)
    {
        return new BuyClaim(args);
    }

    public static BuyClaim valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
