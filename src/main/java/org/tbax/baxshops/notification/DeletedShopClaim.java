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
package org.tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.bukkit.notification.Claimable;
import org.tbax.bukkit.serialization.SafeMap;
import org.tbax.baxshops.serialization.UpgradeableSerializable;
import org.tbax.baxshops.serialization.UpgradeableSerialization;
import org.tbax.bukkit.serialization.annotations.DoNotSerialize;
import org.tbax.bukkit.serialization.annotations.SerializeMethod;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class DeletedShopClaim implements UpgradeableSerializable, Claimable
{
    @SerializeMethod(getter = "getOwner")
    private UUID owner;
    private BaxEntry entry;
    private Date date;

    @Deprecated
    @DoNotSerialize
    private String legacyOwner = null;

    public DeletedShopClaim(Map<String, Object> args)
    {
        UpgradeableSerialization.upgrade(this, args);
    }

    public DeletedShopClaim(OfflinePlayer owner, BaxEntry entry)
    {
        this.owner = owner.getUniqueId();
        this.entry = new BaxEntry(entry);
        this.date = new Date();
    }

    public DeletedShopClaim(UUID owner, BaxEntry entry)
    {
        this(ShopPlugin.getOfflinePlayer(owner), entry);
    }

    @Override
    public void upgrade00300(@NotNull SafeMap map)
    {
        entry = map.getBaxEntry("entry");
        legacyOwner = map.getString("owner");
    }

    @Override
    public void upgrade00400(@NotNull SafeMap map)
    {
        entry = map.getBaxEntry("entry");
        owner = map.getUUID("owner");
        date = map.getDate("date");
    }

    public OfflinePlayer getOwner()
    {
        return ShopPlugin.getOfflinePlayer(owner);
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getOwner().equals(sender)) {
            return String.format("The shop that had this entry no longer exists. You have %s outstanding.", entry.getFormattedName());
        }
        else {
            return getMessage();
        }
    }

    @Deprecated
    public String getLegacyOwner()
    {
        return legacyOwner;
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("The shop that had this entry no longer exists. %s has %s outstanding.",
            Format.username(owner), entry.getFormattedName()
        );
    }

    @Override
    public @NotNull UUID getRecipientId()
    {
        return owner;
    }

    @Override
    public void setRecipient(@NotNull OfflinePlayer player)
    {
        owner = player.getUniqueId();
    }

    @Override
    public Date getSentDate()
    {
        return date;
    }

    @Override
    public void setSentDate(@Nullable Date sentDate)
    {
        date = sentDate;
    }

    public static DeletedShopClaim deserialize(Map<String, Object> args)
    {
        return new DeletedShopClaim(args);
    }

    public static DeletedShopClaim valueOf(Map<String, Object> args)
    {
        return new DeletedShopClaim(args);
    }

    @Override
    public BaxEntry getEntry()
    {
        return entry;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeletedShopClaim that = (DeletedShopClaim) o;
        return Objects.equals(owner, that.owner) &&
                Objects.equals(entry, that.entry);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(owner, entry);
    }

    public void setOwner(@NotNull OfflinePlayer player)
    {
        owner = player.getUniqueId();
    }
}
