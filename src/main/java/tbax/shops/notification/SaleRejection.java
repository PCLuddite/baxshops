package tbax.shops.notification;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tbax.shops.Main;
import tbax.shops.Shop;
import tbax.shops.ShopEntry;
import tbax.shops.Resources;

/**
 * A SaleRejection notifies a seller that his/her offer was rejected.
 */
public class SaleRejection implements Claimable {
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
    public SaleRejection(Shop shop, ShopEntry entry, String seller) {
        this.shop = shop;
        this.entry = entry;
        this.seller = seller;
    }

    @Override
    public String getMessage(Player player) {
        return player == null || !player.getName().equals(seller) ?
                String.format("§1%s §frejected %s's request to sell §e%d %s§F for §a$%.2f§F",
                                shop.owner, seller, entry.quantity, Main.instance.res.getItemName(entry),
                                entry.refundPrice * entry.quantity) :
                String.format("§1%s §frejected your request to sell §e%d %s§F for §a$%.2f§F",
                                shop.owner, entry.quantity, Main.instance.res.getItemName(entry),
                                entry.refundPrice * entry.quantity);
    }

    @Override
    public boolean claim(Player player) {
        ItemStack item = entry.toItemStack();
        if (Resources.inventoryFitsItem(player, item)){
            player.getInventory().addItem(item);
            return true;
        }
        else {
            Main.sendError(player, Resources.NO_ROOM);
            return false;
        }
    }

}
