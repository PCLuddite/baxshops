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
import org.tbax.baxshops.Format;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.serialization.UpgradeableSerializable;
import org.tbax.baxshops.serialization.UpgradeableSerialization;
import org.tbax.baxshops.serialization.annotations.SerializeMethod;
import org.tbax.baxshops.serialization.annotations.SerializedAs;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("unused")
public class DeathTaxReceivedNote implements Notification, UpgradeableSerializable
{
    @SerializedAs("recipient")
    @SerializeMethod(getter = "getRecipient")
    private UUID recipientId;

    @SerializedAs("deceased")
    @SerializeMethod(getter = "getDeadGuy")
    private UUID deadGuyId;

    @SerializedAs("taxed")
    private double deathTax;

    @SerializedAs("message")
    private String msg;

    private Date date;

    public DeathTaxReceivedNote(OfflinePlayer recipient, OfflinePlayer deadGuy, String msg, double death_tax)
    {
        this(recipient.getUniqueId(), deadGuy.getUniqueId(), msg, death_tax);
    }

    public DeathTaxReceivedNote(UUID recipientId, UUID deadGuyId, String msg, double death_tax)
    {
        this.recipientId = recipientId;
        this.deadGuyId = deadGuyId;
        this.msg = msg;
        this.deathTax = death_tax;
        this.date = new Date();
    }

    public DeathTaxReceivedNote(Map<String, Object> args)
    {
        UpgradeableSerialization.upgrade(this, args);
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

    public double getDeathTax()
    {
        return deathTax;
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getRecipient().equals(sender)) {
            return String.format("You received %s because %s",
                Format.money(deathTax), msg
            );
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s received %s because %s was fined for dying.",
            Format.username(recipientId),
            Format.money(deathTax),
            Format.username2(deadGuyId));
    }

    @Override
    public Date getSentDate()
    {
        return date;
    }

    public OfflinePlayer getDeadGuy()
    {
        return ShopPlugin.getOfflinePlayer(deadGuyId);
    }

    public String getDeathMessage()
    {
        if (msg == null)
            msg = "";
        return msg;
    }

    public static DeathTaxReceivedNote deserialize(Map<String, Object> args)
    {
        return new DeathTaxReceivedNote(args);
    }

    public static DeathTaxReceivedNote valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeathTaxReceivedNote that = (DeathTaxReceivedNote) o;
        return Double.compare(that.deathTax, deathTax) == 0 &&
                Objects.equals(recipientId, that.recipientId) &&
                Objects.equals(deadGuyId, that.deadGuyId) &&
                Objects.equals(msg, that.msg);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(recipientId, deadGuyId, deathTax, msg);
    }
}
