package tbax.shops.notification;

import org.bukkit.entity.Player;

import tbax.shops.Main;
import tbax.shops.Resources;
import tbax.shops.Shop;
import tbax.shops.ShopEntry;

/**
 * A SaleNotification notifies a player that his/her sale of an
 * item was successful.
 */
public class SaleNotificationAuto implements Claimable {
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
	public SaleNotificationAuto(Shop shop, ShopEntry entry, String seller) {
            this.shop = shop;
            this.entry = entry;
            this.seller = seller;
	}
        
        public boolean claim(Player player) {
            if (Resources.giveToPlayer(player, entry.toItemStack())) {
                if (entry.quantity == 1) {
                    player.sendMessage("§fThe items have been added to your inventory.");
                }
                else {
                    player.sendMessage("§fThe item has been added to your inventory.");
                }
                return true;
            }
            else {
                return false;
            }
        }
	
	@Override
	public String getMessage(Player player) {
            return getMessage(player == null ? null : player.getName(), shop, entry, seller);
            
	}
        
        public static String getMessage(String buyer, Shop shop, ShopEntry entry, String seller) {
            if (buyer == null || !buyer.equals(shop.owner)) {
                return String.format("§1%s §fsold §1%s §e%d %s§F for §a$%.2f§F",
                            seller, shop.owner, entry.quantity, Main.instance.res.getItemName(entry),
                            entry.refundPrice * entry.quantity);
            }
            else {
                return String.format("§1%s §fsold you §e%d %s§f for §a$%.2f§F",
                            seller, entry.quantity, Main.instance.res.getItemName(entry),
                            entry.refundPrice * entry.quantity);
            }
        }
}
