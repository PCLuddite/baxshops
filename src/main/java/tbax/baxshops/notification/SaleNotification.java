/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.serialization.ItemNames;

/**
 * A SaleNotification notifies a player that his/her sale of an
 * item was successful.
 */
public final class SaleNotification implements ConfigurationSerializable, Notification 
{
    /**
     * An entry for the offered item
     */
    public BaxEntry entry;
    /**
     * The seller of the item
     */
    public String seller;
    /**
     * The buyer of the item
     */
    public String buyer;

    public SaleNotification(Map<String, Object> args)
    {
        this.seller = (String)args.get("seller");
        this.entry = (BaxEntry)args.get("entry");
        if (args.containsKey("shop")) {
            BaxShop shop = Main.getState().getShop((int)args.get("shop"));
            if (shop == null) {
                buyer = Resources.ERROR_INLINE;
            }
            else {
                buyer = shop.getOwner();
            }
        }
        else {
            buyer = (String)args.get("buyer");
        }
    }
    
    /**
     * Constructs a new notification.
     * @param buyer the buyer of the item
     * @param entry an entry for the item (note: not the one in the shop)
     * @param seller the seller of the item
     */
    public SaleNotification(String buyer, String seller, BaxEntry entry) 
    {
        this.buyer = buyer;
        this.entry = entry;
        this.seller = seller;
    }

    @Override
    public String getMessage(Player player) 
    {
        if (player == null || !player.getName().equals(seller)) {
            return String.format("%s accepted %s's request to sell %s for %s.",
                        Format.username(buyer), 
                        Format.username2(seller),
                        Format.itemname(entry.getAmount(), ItemNames.getName(entry)),
                        Format.money(entry.getRefundPrice() * entry.getAmount())
                    );
        }
        else {
            return String.format("%s accepted your request to sell %s for %s.",
                        Format.username(buyer), 
                        Format.itemname(entry.getAmount(), ItemNames.getName(entry)),
                        Format.money(entry.getRefundPrice() * entry.getAmount())
                    );
        }
    }
    
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("seller", seller);
        args.put("buyer", buyer);
        args.put("entry", entry);
        return args;
    }
    
    public static SaleNotification deserialize(Map<String, Object> args)
    {
        return new SaleNotification(args);
    }
    
    public static SaleNotification valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    @Override
    public boolean checkIntegrity()
    {
        return true;
    }
}