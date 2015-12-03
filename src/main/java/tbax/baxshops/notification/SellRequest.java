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
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.Main;
import static tbax.baxshops.Main.sendError;
import tbax.baxshops.Resources;
import tbax.baxshops.serialization.ItemNames;

/**
 * A SellRequest notifies a shop owner that someone has requested
 * to sell him/her an item.
 * SellRequests expire after five days.
 */
public final class SellRequest implements ConfigurationSerializable, Request, TimedNotification
{
    /**
     * An entry for the offered item
     */
    public BaxEntry entry;
    /**
     * The shop to which the item is being sold
     */
    public long shopid;
    /**
     * The date at which the request expires
     */
    public long expirationDate;
    /**
     * The seller of the item
     */
    public String seller;
    /**
     * The buyer of the item
     */
    public String buyer;
    
    public SellRequest(Map<String, Object> args)
    {
        seller = (String)args.get("seller");
        shopid = (int)args.get("shop");
        if (args.containsKey("buyer")) {
            buyer = (String)args.get("buyer");
        }
        entry = (BaxEntry)args.get("entry");
        expirationDate = (long)args.get("expires");
    }
    
    /**
     * Constructs a new notification.
     * @param shopid the shop to which the seller was selling
     * @param buyer
     * @param entry an entry for the item (note: not the one in the shop)
     * @param seller the seller of the item
     */
    public SellRequest(long shopid, String buyer, String seller, BaxEntry entry) 
    {
        this.shopid = shopid;
        this.entry = entry;
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
        if (player == null || !player.getName().equals(buyer)) {
            return String.format("%s wants to sell %s to %s for %s.",
                Format.username(seller), 
                Format.itemname(entry.getAmount(), ItemNames.getName(entry)),
                Format.username2(buyer),
                Format.money(entry.refundPrice * entry.getAmount())
            );
        }
        else {
            return String.format("%s wants to sell you %s for %s.",
                Format.username(seller), 
                Format.itemname(entry.getAmount(), ItemNames.getName(entry)),
                Format.money(entry.refundPrice * entry.getAmount())
            );
        }
    }
    
    @Override
    public boolean accept(Player player)
    {
        double price = Main.roundTwoPlaces(entry.getAmount() * entry.refundPrice);

        ItemStack item = entry.toItemStack();
        
        Economy econ = Main.econ;

        if (!econ.has(buyer, price)) {
            Main.sendError(player, Resources.NO_MONEY);
            return false;
        }
        
        econ.withdrawPlayer(buyer, price);
        econ.depositPlayer(seller, price);

        BaxShop shop = Main.getState().getShop(shopid);
        if (shop == null) {
            DeletedShopClaim shopDeleted = new DeletedShopClaim(buyer, entry);
            Main.getState().sendNotification(player, shopDeleted);
            return true;
        }
        else if (shop.sellToShop) {
            if (!sellToShop(shop, item, player)) {
                return false;
            }
        }
        else if (!Main.tryGiveItem(player, item)) {
            sendError(player, Resources.NO_ROOM);
            return false;
        }

        SaleNotification n = new SaleNotification(shop.owner, seller, entry);
        Main.getState().sendNotification(seller, n);
        
        player.sendMessage("Offer accepted");
        player.sendMessage(String.format(Resources.CURRENT_BALANCE, Format.money2(Main.econ.getBalance(player.getName()))));
        
        return true;
    }
    
    /**
     * Auto-accepts the sale
     * @param sender
     * @return 1 if success, 0 if insufficient funds, -1 if invalid item
     */
    public int autoAccept(CommandSender sender)
    {
        double price = Main.roundTwoPlaces(entry.getAmount() * entry.refundPrice);
        
        Economy econ = Main.econ;
        
        if (!econ.has(buyer, price)) {
            return 0;
        }
        
        econ.withdrawPlayer(buyer, price);
        econ.depositPlayer(seller, price);
        
        Notification buyerNote;
        
        BaxShop shop = Main.getState().getShop(shopid);
        if (shop == null) {
            DeletedShopClaim shopDeleted = new DeletedShopClaim(buyer, entry);
            Main.getState().sendNotification(buyer, shopDeleted);
            return 1;
        }
        else if (shop.sellToShop) {
            if (!sellToShop(shop, entry.toItemStack(), sender)) {
                return -1;
            }
            buyerNote = new SaleNotificationAuto(shop.owner, seller, entry);
            Main.getState().sendNotification(shop.owner, buyerNote);
        }
        else {
            buyerNote = new SaleNotificationAutoClaim(buyer, seller, entry);
            Main.getState().sendNotification(shop.owner, buyerNote);
        }
        return 1;
    }
    
    private boolean sellToShop(BaxShop shop, ItemStack item, CommandSender sender)
    {
        BaxEntry shopEntry = shop.findEntry(item);
        if (shopEntry == null) {
            shopEntry = new BaxEntry();
            shopEntry.setItem(item);
        }
        if (shop.infinite) {
            shopEntry.infinite = true;
        }
        else {
            shopEntry.add(item.getAmount());
        }
        return true;
    }
    
    @Override
    public boolean reject(Player player)
    {
        SaleRejection n = new SaleRejection(buyer, seller, entry);
        Main.getState().sendNotification(seller, n);
        player.sendMessage("§cOffer rejected");
        return true;
    }

    @Override
    public long expirationDate()
    {
        return expirationDate;
    }
    
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("seller", seller);
        args.put("buyer", buyer);
        args.put("shop", shopid);
        args.put("entry", entry);
        args.put("expires", expirationDate);
        return args;
    }
    
    public static SellRequest deserialize(Map<String, Object> args)
    {
        return new SellRequest(args);
    }
    
    public static SellRequest valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    @Override
    public boolean checkIntegrity()
    {
        return true;
    }
}