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
package qs.shops.notification;

import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.notification.internal.SaleRejection;
import org.tbax.baxshops.serialization.internal.StateLoader;
import org.tbax.baxshops.serialization.internal.states.State_00000;
import qs.shops.Shop;
import qs.shops.ShopEntry;

import java.util.Calendar;
import java.util.Date;

/**
 * A SellRequest notifies a shop owner that someone has requested
 * to sell him/her an item.
 * SellRequests expire after five days.
 */
public class SellRequest implements Request, TimedNotification {
	private static final long serialVersionUID = 1L;
	/**
	 * An entry for the offered item
	 */
	public ShopEntry entry;
	/**
	 * The shop to which the item is being sold
	 */
	public Shop shop;
	/**
	 * The date at which the request expires
	 */
	public long expirationDate;
	/**
	 * The seller of the item
	 */
	public String seller;
	
	/**
	 * Constructs a new notification.
	 * @param shop the shop to which the seller was selling
	 * @param entry an entry for the item (note: not the one in the shop)
	 * @param seller the seller of the item
	 */
	public SellRequest(Shop shop, ShopEntry entry, String seller) {
		this.shop = shop;
		this.entry = entry;
		this.seller = seller;
		
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, 5);
		this.expirationDate = c.getTimeInMillis();
	}

	// begin modified class

	@Override
	public @NotNull Class<? extends org.tbax.baxshops.notification.Notification> getNewNoteClass()
	{
		return SaleRejection.class;
	}

	@Override
	public @NotNull org.tbax.baxshops.notification.Notification getNewNote(StateLoader stateLoader)
	{
		return new SaleRejection(
				((State_00000)stateLoader).registerShop(shop),
				((State_00000)stateLoader).registerPlayer(shop.owner),
				((State_00000)stateLoader).registerPlayer(seller),
				entry.modernize()
		);
	}
}
