/*
 * Copyright © 2012 Nathan Dinsmore and Sam Lazarus
 * Modifications Copyright © Timothy Baxendale
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package tbax.shops.notification;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.serialization.StateLoader;
import org.tbax.baxshops.serialization.states.State_00100;
import org.tbax.baxshops.serialization.states.State_00200;
import tbax.shops.Shop;
import tbax.shops.ShopEntry;

public class SaleRejection implements Claimable
{
    private static final long serialVersionUID = 1L;
    public ShopEntry entry;
    public Shop shop;
    public int shopId;
    public String seller;

    public SaleRejection(final Shop shop, final ShopEntry entry, final String seller) {
        this.shop = shop;
        this.entry = entry;
        this.seller = seller;
    }

    public SaleRejection(final JsonObject o) {
        seller = o.get("seller").getAsString();
        shopId = o.get("shop").getAsInt();
        entry = new ShopEntry(o.get("entry").getAsJsonObject());
    }

    @Override
    public @NotNull Class<? extends org.tbax.baxshops.notification.Notification> getNewNoteClass()
    {
        return org.tbax.baxshops.notification.SaleRejection.class;
    }

    @Override
    public @NotNull org.tbax.baxshops.notification.Notification getNewNote(StateLoader stateLoader)
    {
        if (stateLoader instanceof State_00100) {
            return new org.tbax.baxshops.notification.SaleRejection(
                    ((State_00100) stateLoader).registerShop(shop).getId(),
                    ((State_00100) stateLoader).registerPlayer(shop.owner),
                    ((State_00100) stateLoader).registerPlayer(seller),
                    entry.modernize((State_00100) stateLoader)
            );
        }
        else {
            return new org.tbax.baxshops.notification.SaleRejection(
                    ((State_00200)stateLoader).getShop(shopId).getId(),
                    ((State_00200)stateLoader).registerPlayer(((State_00200)stateLoader).getShopOwner(shopId)),
                    ((State_00200)stateLoader).registerPlayer(seller),
                    entry.modernize((State_00200)stateLoader)
            );
        }
    }
}
