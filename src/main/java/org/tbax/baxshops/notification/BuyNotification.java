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
package org.tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.Format;
import org.tbax.bukkit.MathUtil;
import org.tbax.bukkit.notification.StandardNote;
import org.tbax.bukkit.serialization.SafeMap;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class BuyNotification extends StandardNote
{
    public BuyNotification(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public BuyNotification(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public BuyNotification(Map<String, Object> args)
    {
        super(args);
    }

    @Override
    public void upgrade00300(@NotNull SafeMap map)
    {
        legacyBuyer = map.getString("buyer");
        legacySeller = map.getString("seller");
        shopId = BaxShop.DUMMY_UUID;
        entry = map.getBaxEntry("entry");
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getSeller().equals(sender)) {
            return String.format("%s bought %s from you for %s.",
                Format.username(buyer),
                entry.getFormattedName(),
                Format.money(MathUtil.multiply(entry.getRetailPrice(), entry.getAmount()))
            );
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s bought %s from %s for %s.",
            Format.username(buyer),
            entry.getFormattedName(),
            Format.username2(seller),
            Format.money(MathUtil.multiply(entry.getRetailPrice(), entry.getAmount()))
        );
    }

    @Override
    public @NotNull UUID getRecipientId()
    {
        return seller;
    }

    @Override
    public void setRecipient(@NotNull OfflinePlayer player)
    {
        seller = player.getUniqueId();
    }

    public static BuyNotification deserialize(Map<String, Object> args)
    {
        return new BuyNotification(args);
    }

    public static BuyNotification valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
