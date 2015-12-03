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
package tbax.baxshops.notification;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.serialization.ItemNames;

/**
 * A BuyRequest notifies a shop owner that someone has requested
 * to buy an item.
 * BuyRequests expire after five days.
 */
public final class BuyRequest implements Request, TimedNotification
{
    /**
     * An entry for the purchased item
     */
    public BaxEntry purchased;
    /**
     * The shop id at which the item is being purchased
     */
    public long shopid;
    /**
     * The date at which the request expires
     */
    public long expirationDate;
    /**
     *  The user who sold the item
     */
    public String seller;
    /**
     * The buyer of the item
     */
    public String buyer;
    
    public BuyRequest(Map<String, Object> args)
    {
        buyer = (String)args.get("buyer");
        shopid = (int)args.get("shop");
        purchased = (BaxEntry)args.get("entry");
        if (args.containsKey("seller")) {
            seller = (String)args.get("seller");
        }
        expirationDate = (long)args.get("expires");
    }

    /**
     * Constructs a new notification.
     * @param shop the shop id at which the item was being sold
     * @param seller the seller of the item
     * @param entry an entry for the item (note: not the one in the shop)
     * @param buyer the buyer of the item
     */
    public BuyRequest(long shop, String buyer, String seller, BaxEntry entry)
    {
        this.shopid = shop;
        this.purchased = entry;
        this.buyer = buyer;
        this.seller = seller;

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, Resources.EXPIRE_TIME_DAYS);
        this.expirationDate = c.getTimeInMillis();
    }
	
    @Override
    public String getMessage(Player player)
    {
        if (player == null || !player.getName().equals(seller)) {
            return String.format("%s wants to buy %s from %s for %s.",
                        Format.username(buyer),
                        Format.itemname(purchased.getAmount(), ItemNames.getName(purchased)),
                        Format.username2(seller),
                        Format.money(purchased.retailPrice * purchased.getAmount())
                    );
        }
        else {
            return String.format("%s wants to buy %s from you for %s.",
                        Format.username(buyer),
                        Format.itemname(purchased.getAmount(), ItemNames.getName(purchased)),
                        Format.money(purchased.retailPrice * purchased.getAmount())
                    );
        }
    }
	
    @Override
    public boolean accept(Player acceptingPlayer)
    {            
        double price = Main.roundTwoPlaces(purchased.getAmount() * purchased.refundPrice);

        Economy econ = Main.econ;
        
        econ.withdrawPlayer(buyer, price);
        econ.depositPlayer(seller, price);
        
        BuyClaim n = new BuyClaim(seller, purchased, buyer);
        Main.instance.state.sendNotification(buyer, n);
        
        acceptingPlayer.sendMessage("Offer accepted");
        acceptingPlayer.sendMessage(String.format(Resources.CURRENT_BALANCE, Format.money2(Main.econ.getBalance(acceptingPlayer.getName()))));
        return true;
    }
	
    @Override
    public boolean reject(Player player)
    {
        BaxShop shop = Main.instance.state.getShop(shopid);
        if (shop == null) {
            DeletedShopClaim shopDeleted = new DeletedShopClaim(buyer, purchased);
            Main.instance.state.sendNotification(player, shopDeleted);
            return true;
        }
        else if (!shop.infinite) {
            BaxEntry shopEntry = shop.findEntry(purchased.getItemStack());
            if (shopEntry == null) {
                shop.addEntry(purchased);
            }
            else {
                shopEntry.add(purchased.getAmount());
            }
        }

        BuyRejection n = new BuyRejection(seller, buyer, purchased);
        Main.instance.state.sendNotification(buyer, n);
        player.sendMessage("Offer rejected");
        return true;
    }

    @Override
    public long expirationDate()
    {
        return expirationDate;
    }
    
    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("buyer", buyer);
        args.put("seller", seller);
        args.put("shop", shopid);
        args.put("entry", purchased);
        args.put("expires", expirationDate);
        return args;
    }
    
    public static BuyRequest deserialize(Map<String, Object> args)
    {
        return new BuyRequest(args);
    }
    
    public static BuyRequest valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    @Override
    public boolean checkIntegrity()
    {
        return true;
    }
}