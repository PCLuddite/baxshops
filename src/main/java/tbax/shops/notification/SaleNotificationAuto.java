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
import org.tbax.baxshops.notification.SaleClaim;
import org.tbax.baxshops.notification.SaleNotificationAutoClaim;
import org.tbax.baxshops.serialization.StateLoader;
import org.tbax.baxshops.serialization.states.State_00200;
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

    public SaleNotificationAuto(final BaxShop shop, final ShopEntry entry, final String seller) {
        this.shop = shop;
        this.entry = entry;
        this.seller = seller;
    }

    public SaleNotificationAuto(JsonObject o) {
        seller = o.get("seller").getAsString();
        shopId = o.get("shop").getAsInt();
        entry = new ShopEntry(o.get("entry").getAsJsonObject());
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
        return org.tbax.baxshops.notification.SaleClaim.class;
    }
}
