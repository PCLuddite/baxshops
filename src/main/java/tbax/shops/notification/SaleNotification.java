package tbax.shops.notification;

import org.bukkit.entity.Player;

import tbax.shops.Main;
import tbax.shops.Shop;
import tbax.shops.ShopEntry;

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
            return player == null || !player.getName().equals(seller) ?
                    String.format("§1%s §faccepted %s's request to sell §e%d %s§F for §a$%.2f§F",
                                    shop.owner, seller, entry.quantity, Main.instance.res.getItemName(entry),
                                    entry.refundPrice * entry.quantity) :
                    String.format("§1%s §faccepted your request to sell §e%d %s§F for §a$%.2f§F",
                                    shop.owner, entry.quantity, Main.instance.res.getItemName(entry),
                                    entry.refundPrice * entry.quantity);
	}

}
