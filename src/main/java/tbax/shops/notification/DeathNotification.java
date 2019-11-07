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
package tbax.shops.notification;

import com.google.gson.JsonObject;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.notification.GeneralNotification;
import org.tbax.baxshops.serialization.StateLoader;
import org.tbax.baxshops.serialization.states.State_00200;

public class DeathNotification implements Notification
{
    private static final long serialVersionUID = 1L;
    public double tax;
    public String person;
    public static final String JSON_TYPE_ID = "DeathNote";

    public DeathNotification(final String person, final double tax)
    {
        this.person = person;
        this.tax = tax;
    }

    public DeathNotification(JsonObject o)
    {
        tax = o.get("tax").getAsDouble();
        person = o.get("person").getAsString();
    }

    public String getMessage(OfflinePlayer player)
    {
        if (player == null || !player.getName().equals(this.person))
            return String.format("%s was fined $%.2f for dying.", this.person, this.tax);
        return String.format("§FYou were fined §a$%.2f§F for dying.", this.tax);
    }

    @Override
    public @NotNull org.tbax.baxshops.notification.Notification getNewNote(StateLoader stateLoader)
    {
        OfflinePlayer player = ((State_00200)stateLoader).registerPlayer(person);
        GeneralNotification n = new GeneralNotification(getMessage(player));
        n.setRecipient(player);
        return n;
    }

    @Override
    public @NotNull Class<? extends org.tbax.baxshops.notification.Notification> getNewNoteClass()
    {
        return GeneralNotification.class;
    }
}
