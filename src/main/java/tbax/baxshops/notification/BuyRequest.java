/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import tbax.baxshops.*;
import tbax.baxshops.commands.ShopCmdActor;
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
                        Format.money(purchased.getRetailPrice() * purchased.getAmount())
                    );
        }
        else {
            return String.format("%s wants to buy %s from you for %s.",
                        Format.username(buyer),
                        Format.itemname(purchased.getAmount(), ItemNames.getName(purchased)),
                        Format.money(purchased.getRetailPrice() * purchased.getAmount())
                    );
        }
    }
	
    @Override
    public boolean accept(ShopCmdActor actor)
    {            
        double price = MathUtil.multiply(purchased.getAmount(), purchased.getRetailPrice());

        Economy econ = Main.getEconomy();
        
        econ.withdrawPlayer(buyer, price);
        econ.depositPlayer(seller, price);
        
        BuyClaim n = new BuyClaim(seller, purchased, buyer);
        Main.getState().sendNotification(buyer, n);
        
        actor.sendMessage("Offer accepted");
        actor.sendMessage(String.format(Resources.CURRENT_BALANCE, Format.money2(Main.getEconomy().getBalance(actor.getPlayer()))));
        return true;
    }
	
    @Override
    public boolean reject(ShopCmdActor actor)
    {
        BaxShop shop = Main.getState().getShop(shopid);
        if (shop == null) {
            DeletedShopClaim shopDeleted = new DeletedShopClaim(buyer, purchased);
            Main.getState().sendNotification(actor, shopDeleted);
            return true;
        }
        else if (!shop.hasFlagInfinite()) {
            BaxEntry shopEntry = shop.findEntry(purchased.getItemStack());
            if (shopEntry == null) {
                shop.addEntry(purchased);
            }
            else {
                shopEntry.add(purchased.getAmount());
            }
        }

        BuyRejection n = new BuyRejection(seller, buyer, purchased);
        Main.getState().sendNotification(buyer, n);
        actor.sendMessage("Offer rejected");
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