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
import org.tbax.baxshops.internal.notification.BuyNotification;
import org.tbax.baxshops.internal.serialization.StateLoader;
import org.tbax.baxshops.internal.serialization.states.StateLoader_00100;
import org.tbax.baxshops.internal.serialization.states.StateLoader_00200;
import org.tbax.baxshops.internal.serialization.states.StateLoader_00205;
import org.tbax.baxshops.internal.serialization.states.StateLoader_00210;
import tbax.shops.Shop;
import tbax.shops.ShopEntry;

@Deprecated
public class SaleNotification implements Notification
{
    private static final long serialVersionUID = 1L;
    public ShopEntry entry;
    public Shop shop;
    public int shopId;
    public String seller;

    public static final String JSON_TYPE_ID = "SaleNote";

    public SaleNotification(final Shop shop, final ShopEntry entry, final String seller) {
        this.shop = shop;
        this.entry = entry;
        this.seller = seller;
    }

    public SaleNotification(StateLoader_00200 state00200, JsonObject o) {
        seller = o.get("seller").getAsString();
        shopId = o.get("shop").getAsInt();
        if (state00200 instanceof StateLoader_00210) {
            entry = new ShopEntry((StateLoader_00210)state00200, o.get("entry").getAsJsonObject());
        }
        else if (state00200 instanceof StateLoader_00205) {
            entry = new ShopEntry((StateLoader_00205)state00200, o.get("entry").getAsJsonObject());
        }
        else {
            entry = new ShopEntry(state00200, o.get("entry").getAsJsonObject());
        }
    }

    @Override
    public @NotNull Class<? extends org.tbax.baxshops.notification.Notification> getNewNoteClass()
    {
        return BuyNotification.class;
    }

    @Override
    public @NotNull org.tbax.baxshops.notification.Notification getNewNote(StateLoader stateLoader)
    {
        if (stateLoader instanceof StateLoader_00100) {
            return new BuyNotification(
                    ((StateLoader_00100)stateLoader).registerShop(shop).getId(),
                    ((StateLoader_00100)stateLoader).registerPlayer(shop.owner),
                    ((StateLoader_00100)stateLoader).registerPlayer(seller),
                    entry.modernize((StateLoader_00100) stateLoader)
            );
        }
        else {
            return new BuyNotification(
                    ((StateLoader_00200)stateLoader).getShop(shopId).getId(),
                    ((StateLoader_00200)stateLoader).registerPlayer(((StateLoader_00200)stateLoader).getShopOwner(shopId)),
                    ((StateLoader_00200)stateLoader).registerPlayer(seller),
                    entry.modernize((StateLoader_00200)stateLoader)
            );
        }
    }
}
