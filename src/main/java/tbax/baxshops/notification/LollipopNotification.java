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
package tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.UpgradeableSerialization;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.StoredPlayer;
import tbax.baxshops.serialization.UpgradeableSerializable;
import tbax.baxshops.serialization.states.State_00300;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * A LollipopNotification notifies a player that someone sent him/her a
 * lollipop.
 */
public final class LollipopNotification implements Notification, UpgradeableSerializable
{
    public static final double DEFAULT_TASTINESS = 40;
    private static final String[] adjectives =  {
        "a disgusting",
        "a bad",
        "an icky",
        "a bland",
        "a",
        "an OK",
        "a good",
        "a great",
        "a tasty",
        "a delicious",
        "a wonderful"
    };

    private UUID sender;
    private UUID recipient;
    private double tastiness;
    private Date date;

    public LollipopNotification(Map<String, Object> args)
    {
        UpgradeableSerialization.deserialize(this, args);
    }

    public LollipopNotification(OfflinePlayer sender, double tastiness)
    {
        this.sender = sender.getUniqueId();
        this.tastiness = tastiness < 0 ? 0 : tastiness > 100 ? 100 : tastiness;
        this.date = new Date();
    }

    @Override
    public void deserialize00300(@NotNull SafeMap map)
    {
        sender = State_00300.getPlayerId(map.getString("sender"));
        recipient = StoredPlayer.ERROR_UUID;
        tastiness = map.getDouble("tastiness");
    }

    @Override
    public void deserialize00400(@NotNull SafeMap map)
    {
        sender = map.getUUID("sender");
        recipient = StoredPlayer.ERROR_UUID;
        tastiness = map.getDouble("tastiness");
    }

    @Override
    public void deserialize00410(@NotNull SafeMap map)
    {
        sender = map.getUUID("sender");
        recipient = map.getUUID("receiver");
        tastiness = map.getDouble("tastiness");
        date = map.getDate("date");
    }

    public OfflinePlayer getSender()
    {
        return ShopPlugin.getOfflinePlayer(sender);
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getRecipient().equals(sender)) {
            return getSender().getName() + " sent you " + getTastiness() + " lollipop";
        }
        else {
            return getMessage();
        }
    }

    private OfflinePlayer getRecipient()
    {
        return ShopPlugin.getOfflinePlayer(recipient);
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s sent %s %s lollipop", getSender().getName(), getRecipient().getName(), getTastiness());
    }

    @Override
    public Date getSentDate()
    {
        return date;
    }

    public Map<String, Object> serialize()
    {
        SafeMap args = new SafeMap();
        args.put("sender", getSender());
        args.put("recipient", getRecipient());
        args.put("tastiness", tastiness);
        args.put("date", date);
        return args;
    }

    public static LollipopNotification deserialize(Map<String, Object> args)
    {
        return new LollipopNotification(args);
    }

    public static LollipopNotification valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    public String getTastiness()
    {
        if (tastiness >= 55.0 && tastiness < 60.0)
            return "a better-than-average";
        String adjective = "a";
        for (int i = 0; i < adjectives.length; ++i) {
            if (tastiness >= (i * 100)) {
                adjective = adjectives[i];
            }
        }
        return adjective;
    }
}
