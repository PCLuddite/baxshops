/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.*;
import tbax.baxshops.commands.CommandErrorException;
import tbax.baxshops.commands.PrematureAbortException;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.serialization.ItemNames;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
                Format.money(MathUtil.multiply(entry.getRefundPrice(), entry.getAmount()))
            );
        }
        else {
            return String.format("%s wants to sell you %s for %s.",
                Format.username(seller), 
                Format.itemname(entry.getAmount(), ItemNames.getName(entry)),
                Format.money(MathUtil.multiply(entry.getRefundPrice(), entry.getAmount()))
            );
        }
    }
    
    @Override
    public boolean accept(ShopCmdActor actor) throws PrematureAbortException
    {
        double price = MathUtil.multiply(entry.getAmount(), entry.getRefundPrice());

        ItemStack item = entry.toItemStack();
        
        Economy econ = Main.getEconomy();

        if (!econ.has(buyer, price)) {
            actor.exitError(Resources.NO_MONEY);
        }
        
        econ.withdrawPlayer(buyer, price);
        econ.depositPlayer(seller, price);

        BaxShop shop = Main.getState().getShop(shopid);
        if (shop == null) {
            DeletedShopClaim shopDeleted = new DeletedShopClaim(buyer, entry);
            Main.getState().sendNotification(actor.getPlayer(), shopDeleted);
            return true;
        }
        else if (shop.hasFlagSellToShop()) {
            sellToShop(shop, entry.toItemStack());
        }
        actor.giveItem(item);

        SaleNotification n = new SaleNotification(shop.getOwner(), seller, entry);
        Main.getState().sendNotification(seller, n);
        
        actor.sendMessage("Offer accepted");
        actor.sendMessage(Resources.CURRENT_BALANCE, Format.money2(Main.getEconomy().getBalance(actor.getPlayer())));
        
        return true;
    }
    
    /**
     * Auto-accepts the sale
     */
    public void autoAccept(ShopCmdActor actor) throws PrematureAbortException
    {
        double price = MathUtil.multiply(entry.getAmount(), entry.getRefundPrice());

        Economy econ = Main.getEconomy();

        if (!econ.has(buyer, price)) {
            throw new CommandErrorException("You do not have enough money to complete this command.");
        }

        econ.withdrawPlayer(buyer, price);
        econ.depositPlayer(seller, price);

        Notification buyerNote;

        BaxShop shop = Main.getState().getShop(shopid);
        if (shop == null) {
            DeletedShopClaim shopDeleted = new DeletedShopClaim(buyer, entry);
            Main.getState().sendNotification(buyer, shopDeleted);
        }
        else if (shop.hasFlagSellToShop()) {
            sellToShop(shop, entry.toItemStack());
            buyerNote = new SaleNotificationAuto(shop.getOwner(), seller, entry);
            Main.getState().sendNotification(shop.getOwner(), buyerNote);
        }
        else {
            buyerNote = new SaleNotificationAutoClaim(buyer, seller, entry);
            Main.getState().sendNotification(shop.getOwner(), buyerNote);
        }
    }
    
    @Override
    public boolean reject(ShopCmdActor player)
    {
        SaleRejection n = new SaleRejection(buyer, seller, entry);
        Main.getState().sendNotification(seller, n);
        player.sendMessage("§cOffer rejected");
        return true;
    }

    private void sellToShop(BaxShop shop, ItemStack item)
    {
        BaxEntry shopEntry = shop.findEntry(item);
        if (shopEntry == null) {
            shopEntry = new BaxEntry();
            shopEntry.setItem(item);
        }
        if (shop.hasFlagInfinite()) {
            shopEntry.setInfinite(true);
        }
        else {
            shopEntry.add(item.getAmount());
        }
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