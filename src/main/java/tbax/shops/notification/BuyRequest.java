/* 
 * The MIT License
 *
 * Copyright © 2013-2015 Timothy Baxendale (pcluddite@hotmail.com) and 
 * Copyright © 2012 Nathan Dinsmore and Sam Lazarus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
