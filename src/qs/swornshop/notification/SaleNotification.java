package qs.swornshop.notification;

import org.bukkit.entity.Player;

import qs.swornshop.Main;
import qs.swornshop.Shop;
import qs.swornshop.ShopEntry;

/**
 * A SaleNotification notifies a player that his/her sale of an
 * item was successful.
 */
public class SaleNotification implements Notification {
	
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
		return player == null || !player.getName().equals(seller) ?
			String.format("%s accepted %s's request to sell §B%d %s§F for §B$%.2f§F",
					shop.owner, seller, entry.item.getAmount(), Main.instance.getItemName(entry),
					entry.refundPrice * entry.item.getAmount()) :
			String.format("%s accepted your request to sell §B%d %s§F for §B%.2f§F",
					shop.owner, entry.item.getAmount(), Main.instance.getItemName(entry),
					entry.refundPrice * entry.item.getAmount());
	}

}
