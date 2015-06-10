package tbax.shops.notification;

import java.util.Calendar;
import java.util.Date;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tbax.shops.BaxShop;
import tbax.shops.Main;
import static tbax.shops.Main.sendError;
import tbax.shops.Shop;
import tbax.shops.ShopEntry;
import tbax.shops.Resources;

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
        c.add(Calendar.DATE, Resources.EXPIRE_TIME_DAYS);
        this.expirationDate = c.getTimeInMillis();
    }

    @Override
    public String getMessage(Player player) {
        return player == null || !player.getName().equals(shop.owner) ?
                String.format("%s wants to sell %s §e%d %s§F for §a$%.2f§F",
                                seller, shop.owner, entry.quantity, Main.instance.res.getItemName(entry),
                                entry.refundPrice * entry.quantity) :
                String.format("%s wants to sell you §e%d %s§F for §a$%.2f§F",
                                seller, entry.quantity, Main.instance.res.getItemName(entry),
                                entry.refundPrice * entry.quantity);
    }
    
    @Override
    public boolean accept(Player player) {
        
        double price = Resources.roundTwoPlaces((double)entry.quantity * entry.refundPrice);

        ItemStack item = entry.toItemStack();
        
        Economy econ = Main.econ;

        if (!econ.has(shop.owner, price)) {
            Main.sendError(player, Resources.NO_MONEY);
            return false;
        }
        
        econ.withdrawPlayer(shop.owner, price);
        econ.depositPlayer(seller, price);

        if ((boolean)((BaxShop)shop).getOption("sell_to_shop")) {
            sellToShop(item);
        }
        else if (!Resources.giveToPlayer(player, item)) {
            sendError(player, Resources.NO_ROOM);
            return false;
        }

        SaleNotification n = new SaleNotification(shop, entry, seller);
        Main.instance.res.sendNotification(seller, n);
        
        player.sendMessage("§aOffer accepted");
        player.sendMessage(String.format(Resources.CURRENT_BALANCE, Main.econ.format(Main.econ.getBalance(player.getName()))));
        
        return true;
    }
    
    public boolean autoAccept() {
        double price = Resources.roundTwoPlaces((double)entry.quantity * entry.refundPrice);
        
        Economy econ = Main.econ;
        
        if (!econ.has(shop.owner, price)) {
            return false;
        }
        
        econ.withdrawPlayer(shop.owner, price);
        econ.depositPlayer(seller, price);
        
        Notification buyerNote;
        if ((boolean)((BaxShop)shop).getOption("sell_to_shop")) {
            sellToShop(entry.toItemStack());
            buyerNote = new GeneralNotification(
                SaleNotificationAuto.getMessage(shop.owner, shop, entry, seller)
            );
        }
        else {
            buyerNote = new SaleNotificationAuto(shop, entry, seller);
        }
        Main.instance.res.sendNotification(shop.owner, buyerNote);
        return true;
    }
    
    private void sellToShop(ItemStack item) {
        ShopEntry shopEntry = shop.findEntry(item.getTypeId(), item.getDurability());
        if (shopEntry == null) {
            shopEntry = new ShopEntry();
            shopEntry.setItem(item);
            shop.addEntry(shopEntry);
        }
        if (shop.isInfinite) {
            shopEntry.setAmount(-8);
        }
        else {
            shopEntry.setAmount(shopEntry.quantity + item.getAmount());
        }
    }
    
    @Override
    public boolean reject(Player player) {
        SaleRejection n = new SaleRejection(shop, entry, seller);
        Main.instance.res.sendNotification(seller, n);
        player.sendMessage("§cOffer rejected");
        return true;
    }

    @Override
    public long expirationDate() {
        return expirationDate;
    }
	
}
