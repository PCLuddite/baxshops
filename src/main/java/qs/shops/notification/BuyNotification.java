package qs.shops.notification;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.serialization.qs.NathanConverter;
import qs.shops.Shop;
import qs.shops.ShopEntry;

/**
 * A BuyNotification notifies a shop owner that someone bought an item
 * from him/her.
 */
public class BuyNotification implements Notification {
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
	public String buyer;
	
	/**
	 * Constructs a new notification.
	 * @param shop the shop to which the seller was selling
	 * @param entry an entry for the item (note: not the one in the shop)
	 */
	public BuyNotification(Shop shop, ShopEntry entry, String buyer) {
		this.shop = shop;
		this.entry = entry;
		this.buyer = buyer;
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
				NathanConverter.registerPlayer(buyer),
				NathanConverter.registerPlayer(shop.owner),
				BaxEntry.fromNathan(entry)
		);
	}
}
