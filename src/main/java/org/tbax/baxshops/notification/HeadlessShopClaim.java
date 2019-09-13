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
import org.jetbrains.annotations.Nullable;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.serialization.UpgradeableSerializable;
import org.tbax.baxshops.serialization.UpgradeableSerialization;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class HeadlessShopClaim implements Claimable, UpgradeableSerializable
{
    private Date date = new Date();
    private BaxShop shop;

    public HeadlessShopClaim(Map<String, Object> args)
    {
        UpgradeableSerialization.deserialize(this, args);
    }

    public HeadlessShopClaim(@NotNull BaxShop shop)
    {
        this.shop = shop;
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getRecipient().equals(sender)) {
            return "You have a shop with inventory that has no locations.";
        }
        return getMessage();
    }

    @Override
    public @NotNull String getMessage()
    {
        return null;
    }

    @Override
    public @Nullable Date getSentDate()
    {
        return date;
    }

    @Override
    public @NotNull UUID getRecipientId()
    {
        return shop.getOwnerId();
    }

    @Override
    public void setRecipient(@NotNull OfflinePlayer player)
    {
        shop.setOwner(player);
    }

    public static HeadlessShopClaim deserialize(Map<String, Object> args)
    {
        return new HeadlessShopClaim(args);
    }

    public static HeadlessShopClaim valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    @Override
    public BaxEntry getEntry()
    {
        return new BaxEntry(shop.toItem());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (!(obj instanceof HeadlessShopClaim)) return false;
        return shop.getId().equals(((HeadlessShopClaim)obj).shop.getId());
    }
}
