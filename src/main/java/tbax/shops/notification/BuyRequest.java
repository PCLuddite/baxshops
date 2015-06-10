/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tbax.shops.notification;

import java.util.Calendar;
import java.util.Date;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import tbax.shops.Main;
import tbax.shops.Resources;
import tbax.shops.Shop;
import tbax.shops.ShopEntry;

/**
 * A BuyRequest notifies a shop owner that someone has requested
 * to buy an item.
 * BuyRequests expire after five days.
 */
public class BuyRequest implements Request, TimedNotification {
    private static final long serialVersionUID = 1L;
    /**
     * An entry for the purchased item
     */
    public ShopEntry purchased;
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
    public String buyer;

    /**
     * Constructs a new notification.
     * @param shop the shop to which the seller was selling
     * @param entry an entry for the item (note: not the one in the shop)
     * @param buyer the buyer of the item
     */
    public BuyRequest(Shop shop, ShopEntry entry, String buyer) {
        this.shop = shop;
        this.purchased = entry;
        this.buyer = buyer;

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, Resources.EXPIRE_TIME_DAYS);
        this.expirationDate = c.getTimeInMillis();
    }
	
    @Override
    public String getMessage(Player player) {
        return player == null || !player.getName().equals(shop.owner) ?
                String.format("§1%s §fwants to buy from %s §e%d %s§F for §a$%.2f§F",
                                buyer, shop.owner, purchased.quantity, Main.instance.res.getItemName(purchased),
                                purchased.refundPrice * purchased.quantity) :
                String.format("§1%s §fwants to buy §e%d %s§F from you for §a$%.2f§F",
                                buyer, purchased.quantity, Main.instance.res.getItemName(purchased),
                                purchased.refundPrice * purchased.quantity);
    }
	
    @Override
    public boolean accept(Player acceptingPlayer) {            
        double price = Resources.roundTwoPlaces(purchased.quantity * purchased.refundPrice);

        Economy econ = Main.econ;
        
        econ.withdrawPlayer(buyer, price);
        econ.depositPlayer(shop.owner, price);
        
        BuyClaim n = new BuyClaim(shop, purchased, buyer);
        Main.instance.res.sendNotification(buyer, n);
        
        acceptingPlayer.sendMessage("§aOffer accepted");
        acceptingPlayer.sendMessage(String.format(Resources.CURRENT_BALANCE, Main.econ.format(Main.econ.getBalance(acceptingPlayer.getName()))));
        return true;
    }
	
    @Override
    public boolean reject(Player player) {
        if (!shop.isInfinite) {
            ShopEntry shopEntry = shop.findEntry(purchased.itemID, purchased.itemDamage);
            if (shopEntry == null) {
                shop.addEntry(purchased);
            }
            else {
                shopEntry.setAmount(shopEntry.quantity + purchased.quantity);
            }
        }

        BuyRejection n = new BuyRejection(shop, purchased, buyer);
        Main.instance.res.sendNotification(buyer, n);
        player.sendMessage("§cOffer rejected");
        return true;
    }

    @Override
    public long expirationDate() {
        return expirationDate;
    }
}
