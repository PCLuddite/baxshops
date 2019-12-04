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
import org.tbax.baxshops.notification.internal.SaleClaim;
import org.tbax.baxshops.serialization.internal.StateLoader;
import org.tbax.baxshops.serialization.internal.states.State_00200;
import org.tbax.baxshops.serialization.internal.states.State_00205;
import org.tbax.baxshops.serialization.internal.states.State_00210;
import tbax.shops.ShopEntry;
import tbax.shops.BaxShop;

public class SaleNotificationAuto implements Claimable
{
    private static final long serialVersionUID = 1L;
    public ShopEntry entry;
    public int shopId;
    public BaxShop shop;
    public String seller;
    public static final String JSON_TYPE_ID = "SaleNoteAuto";

    public SaleNotificationAuto(BaxShop shop, ShopEntry entry, String seller) {
        this.shop = shop;
        this.entry = entry;
        this.seller = seller;
    }

    public SaleNotificationAuto(State_00200 state00200, JsonObject o) {
        seller = o.get("seller").getAsString();
        shopId = o.get("shop").getAsInt();
        if (state00200 instanceof State_00210) {
            entry = new ShopEntry((State_00210)state00200, o.get("entry").getAsJsonObject());
        }
        else if (state00200 instanceof State_00205) {
            entry = new ShopEntry((State_00205)state00200, o.get("entry").getAsJsonObject());
        }
        else {
            entry = new ShopEntry(state00200, o.get("entry").getAsJsonObject());
        }
    }

    @Override
    public org.tbax.baxshops.notification.@NotNull Notification getNewNote(StateLoader stateLoader)
    {
        return new SaleClaim(((State_00200)stateLoader).getShop(shopId).getId(),
                ((State_00200)stateLoader).registerPlayer(((State_00200)stateLoader).getShopOwner(shopId)),
                ((State_00200)stateLoader).registerPlayer(seller),
                entry.modernize((State_00200)stateLoader)
        );
    }

    @Override
    public @NotNull Class<? extends org.tbax.baxshops.notification.Notification> getNewNoteClass()
    {
        return SaleClaim.class;
    }
}
