package tbax.shops.notification;

import org.bukkit.entity.Player;
import tbax.shops.Main;
import tbax.shops.Shop;
import tbax.shops.ShopEntry;

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
     * @param buyer the buyer of the item
     */
    public BuyNotification(Shop shop, ShopEntry entry, String buyer) {
        this.shop = shop;
        this.entry = entry;
        this.buyer = buyer;
    }
    
    @Override
    public String getMessage(Player player) {
        if (player == null || !player.getName().equals(shop.owner)) {
            return String.format("§1%s §fbought §e%d %s§F from %s for §a$%.2f§F",
                        buyer, entry.quantity, Main.instance.res.getItemName(entry),
                        shop.owner, entry.retailPrice * entry.quantity);
        }
        else {
            return String.format("§1%s §fbought §e%d %s§F from you for §a$%.2f§F",
                            buyer, entry.quantity, Main.instance.res.getItemName(entry),
                            entry.retailPrice * entry.quantity);
        }
    }

}
