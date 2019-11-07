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
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.serialization.StateLoader;
import org.tbax.baxshops.serialization.StoredPlayer;
import org.tbax.baxshops.serialization.states.State_00200;
import tbax.shops.ShopEntry;

public class BuyRejection implements Notification
{
    public ShopEntry entry;
    public int shopId;
    public String seller;
    public static final String JSON_TYPE_ID = "BuyReject";

    public BuyRejection(JsonObject o)
    {
        seller = o.get("seller").getAsString();
        shopId = o.get("shop").getAsInt();
        entry = new ShopEntry(o.get("entry").getAsJsonObject());
    }

    @Override
    public @NotNull org.tbax.baxshops.notification.Notification getNewNote(StateLoader stateLoader)
    {
        return new org.tbax.baxshops.notification.BuyRejection(
                ((State_00200)stateLoader).getShop(shopId).getId(),
                ((State_00200)stateLoader).getShop(shopId).getOwner(),
                ((State_00200)stateLoader).registerPlayer(seller),
                entry.modernize((State_00200)stateLoader)
        );
    }

    @Override
    public @NotNull Class<? extends org.tbax.baxshops.notification.Notification> getNewNoteClass()
    {
        return org.tbax.baxshops.notification.BuyRejection.class;
    }
}
