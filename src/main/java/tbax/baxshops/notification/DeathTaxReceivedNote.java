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
package tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.Format;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.SafeMap;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public class DeathTaxReceivedNote implements Notification
{
    private UUID recipientId;
    private UUID deadGuyId;
    private double deathTax;
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
        SafeMap map = new SafeMap(args);
        recipientId = map.getUUID("recipient");
        deadGuyId = map.getUUID("deceased");
        deathTax = map.getDouble("taxed");
        msg = map.getString("message");
        date = map.getDate("date");
    }

    public OfflinePlayer getRecipient()
    {
        return ShopPlugin.getOfflinePlayer(recipientId);
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

    @Override
    public Map<String, Object> serialize()
    {
        SafeMap args = new SafeMap();
        args.put("recipient", getRecipient());
        args.put("deceased", getDeadGuy());
        args.put("taxed", deathTax);
        args.put("message", msg);
        args.put("date", date);
        return args;
    }

    public OfflinePlayer getDeadGuy()
    {
        return ShopPlugin.getOfflinePlayer(deadGuyId);
    }

    public String getDeathMessage()
    {
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
}
