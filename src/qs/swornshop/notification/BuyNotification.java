package qs.swornshop.notification;

import org.bukkit.entity.Player;

import qs.swornshop.Main;
import qs.swornshop.Shop;
import qs.swornshop.ShopEntry;

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
	 * @param seller the seller of the item
	 */
	public BuyNotification(Shop shop, ShopEntry entry, String buyer) {
		this.shop = shop;
		this.entry = entry;
		this.buyer = buyer;
	}
	
	@Override
	public String getMessage(Player player) {
		return player == null || !player.getName().equals(shop.owner) ?
			String.format("%s bought §B%d %s§F from %s for §B$%.2f§F",
					buyer, entry.item.getAmount(), Main.instance.getItemName(entry),
					shop.owner, entry.retailPrice * entry.item.getAmount()) :
			String.format("%s bought §B%d %s§F from you for §B$%.2f§F",
					buyer, entry.item.getAmount(), Main.instance.getItemName(entry),
					entry.retailPrice * entry.item.getAmount());
	}

}
