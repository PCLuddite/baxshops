/*
 * Copyright (c) Timothy Baxendale
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
import org.tbax.baxshops.serialization.UpgradeableSerializable;
import org.tbax.baxshops.serialization.UpgradeableSerialization;
import org.tbax.baxshops.serialization.annotations.SerializeMethod;
import org.tbax.baxshops.serialization.annotations.SerializedAs;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class GeneralNotification implements Notification, UpgradeableSerializable
{

    @SerializedAs("recipient")
    @SerializeMethod(getter = "getRecipient")
    private UUID recipientId;

    private String message;
    private Date date;

    public GeneralNotification(String message)
    {
        date = new Date();
        this.message = message;
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        return message;
    }

    @Override
    public @NotNull String getMessage()
    {
        return message;
    }

    @Override
    public @Nullable Date getSentDate()
    {
        return date;
    }

    @Override
    public @NotNull UUID getRecipientId()
    {
        return recipientId;
    }

    @Override
    public void setRecipient(@NotNull OfflinePlayer player)
    {
        recipientId = player.getUniqueId();
    }

    @Override
    public @NotNull Map<String, Object> serialize()
    {
        return UpgradeableSerialization.serialize(this);
    }
}
