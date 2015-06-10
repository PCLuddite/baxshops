/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tbax.baxshops.notification;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Calendar;
import java.util.Date;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.serialization.ItemNames;

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
    public BaxEntry purchased;
    /**
     * The shop to which the item is being sold
     */
    public BaxShop shop;
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
    public BuyRequest(BaxShop shop, BaxEntry entry, String buyer) {
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
                String.format("%s wants to buy from %s %d %s for $%.2f",
                                buyer, shop.owner, purchased.getAmount(), ItemNames.getItemName(purchased),
                                purchased.refundPrice * purchased.getAmount()) :
                String.format("§1%s §fwants to buy §e%d %s§F from you for §a$%.2f§F",
                                buyer, purchased.getAmount(), ItemNames.getItemName(purchased),
                                purchased.refundPrice * purchased.getAmount());
    }
	
    @Override
    public boolean accept(Player acceptingPlayer) {            
        double price = Main.roundTwoPlaces(purchased.getAmount() * purchased.refundPrice);

        Economy econ = Main.econ;
        
        econ.withdrawPlayer(buyer, price);
        econ.depositPlayer(shop.owner, price);
        
        BuyClaim n = new BuyClaim(shop, purchased, buyer);
        Main.instance.state.sendNotification(buyer, n);
        
        acceptingPlayer.sendMessage("§aOffer accepted");
        acceptingPlayer.sendMessage(String.format(Resources.CURRENT_BALANCE, Main.econ.format(Main.econ.getBalance(acceptingPlayer.getName()))));
        return true;
    }
	
    @Override
    public boolean reject(Player player) {
        if (!shop.infinite) {
            BaxEntry shopEntry = shop.findEntry(purchased.getType(), purchased.getDurability());
            if (shopEntry == null) {
                shop.addEntry(purchased);
            }
            else {
                shopEntry.add(purchased.getAmount());
            }
        }

        BuyRejection n = new BuyRejection(shop, purchased, buyer);
        Main.instance.state.sendNotification(buyer, n);
        player.sendMessage("§cOffer rejected");
        return true;
    }

    @Override
    public long expirationDate() {
        return expirationDate;
    }
    
    public static final String TYPE_ID = "BuyRequest";
    
    @Override
    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type", TYPE_ID);
        o.addProperty("buyer", buyer);
        o.addProperty("shop", shop.uid);
        o.addProperty("expires", expirationDate);
        o.add("entry", purchased.toJson());
        return o;
    }
    
    public BuyRequest() {
    }
    
    public static BuyRequest fromJson(JsonObject o) {
        BuyRequest request = new BuyRequest();
        request.buyer = o.get("buyer").getAsString();
        request.shop = Main.instance.state.getShop(o.get("shop").getAsInt());
        request.expirationDate = o.get("expires").getAsLong();
        request.purchased = new BaxEntry(o.get("entry").getAsJsonObject());
        return request;
    }
}
