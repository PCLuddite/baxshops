package qs.shops.notification;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.serialization.qs.NathanConverter;
import qs.shops.Shop;
import qs.shops.ShopEntry;

/**
 * A SaleNotification notifies a player that his/her sale of an
 * item was successful.
 */
public class SaleNotification implements Notification {
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
	 * The seller of the item
	 */
	public String seller;
	
	/**
	 * Constructs a new notification.
	 * @param shop the shop to which the seller was selling
	 * @param entry an entry for the item (note: not the one in the shop)
	 * @param seller the seller of the item
	 */
	public SaleNotification(Shop shop, ShopEntry entry, String seller) {
		this.shop = shop;
		this.entry = entry;
		this.seller = seller;
	}
	
	@Override
	public String getMessage(Player player) {
		return null;
	}

	// begin modified class

	@Override
	public @NotNull Class<? extends org.tbax.baxshops.notification.Notification> getNewNoteClass()
	{
		return org.tbax.baxshops.notification.BuyNotification.class;
	}

	@Override
	public @NotNull org.tbax.baxshops.notification.Notification getNewNote()
	{
		return new org.tbax.baxshops.notification.BuyNotification(
				NathanConverter.registerShop(shop),
				NathanConverter.registerPlayer(shop.owner),
				NathanConverter.registerPlayer(seller),
				BaxEntry.fromNathan(entry)
		);
	}
}
