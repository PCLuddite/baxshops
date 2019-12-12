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
import org.tbax.baxshops.internal.notification.SaleClaim;
import org.tbax.baxshops.internal.serialization.StateLoader;
import org.tbax.baxshops.internal.serialization.states.StateLoader_00100;
import org.tbax.baxshops.internal.serialization.states.StateLoader_00200;
import org.tbax.baxshops.internal.serialization.states.StateLoader_00210;
import tbax.shops.ShopEntry;
import tbax.shops.BaxShop;

@Deprecated
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

    public SaleNotificationAuto(StateLoader_00100 state00200, JsonObject o) {
        seller = o.get("seller").getAsString();
        shopId = o.get("shop").getAsInt();
        if (state00200 instanceof StateLoader_00210) {
            entry = new ShopEntry((StateLoader_00210)state00200, o.get("entry").getAsJsonObject());
        }
        else if (state00200 instanceof StateLoader_00200) {
            entry = new ShopEntry((StateLoader_00200)state00200, o.get("entry").getAsJsonObject());
        }
        else {
            entry = new ShopEntry(state00200, o.get("entry").getAsJsonObject());
        }
    }

    @Override
    public org.tbax.baxshops.notification.@NotNull Notification getNewNote(StateLoader stateLoader)
    {
        return new SaleClaim(((StateLoader_00100)stateLoader).getShop(shopId).getId(),
                ((StateLoader_00100)stateLoader).registerPlayer(((StateLoader_00100)stateLoader).getShopOwner(shopId)),
                ((StateLoader_00100)stateLoader).registerPlayer(seller),
                entry.modernize((StateLoader_00100)stateLoader)
        );
    }

    @Override
    public @NotNull Class<? extends org.tbax.baxshops.notification.Notification> getNewNoteClass()
    {
        return SaleClaim.class;
    }
}
