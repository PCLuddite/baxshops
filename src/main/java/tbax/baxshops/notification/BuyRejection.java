/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.serialization.ItemNames;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public final class BuyRejection implements Notification 
{
    /**
     * An entry for the offered item
     */
    public BaxEntry entry;
    /**
     *  The user who sold the item
     */
    public String seller;
    /**
     * The buyer of the item
     */
    public String buyer;
    
    public BuyRejection(Map<String, Object> args)
    {
        if (args.containsKey("buyer")) {
            this.buyer = (String)args.get("buyer");
            this.seller = (String)args.get("seller");
        }
        else {
            this.buyer = (String)args.get("seller");
            BaxShop shop = Main.getState().getShop((int)args.get("shop"));
            if (shop == null) {
                seller = Resources.ERROR_INLINE;
            }
            else {
                seller = shop.getOwner();
            }
        }
        this.entry = (BaxEntry)args.get("entry");
    }

    /**
     * Constructs a new notification.
     * @param seller the username of the seller
     * @param entry an entry for the item (note: not the one in the shop)
     * @param buyer the buyer of the item
     */
    public BuyRejection(String seller, String buyer, BaxEntry entry)
    {
        this.entry = entry;
        this.seller = seller;
        this.buyer = buyer;
    }

    @Override
    public String getMessage(ShopCmdActor actor)
    {
        if (actor.getPlayer() == null || !actor.getPlayer().getName().equals(seller)) {
            return String.format("%s " + ChatColor.RED + "rejected" + ChatColor.RESET + " %s's request to buy %s for %s.",
                        Format.username(seller),
                        Format.username2(buyer),
                        Format.itemname(entry.getAmount(), ItemNames.getName(entry)),
                        Format.money(entry.getRetailPrice() * entry.getAmount())
                    );
        }
        else {
            return String.format("%s " + ChatColor.RED + "rejected" + ChatColor.RESET + " your request to buy %s for %s.",
                        Format.username(seller),
                        Format.itemname(entry.getAmount(), ItemNames.getName(entry)),
                        Format.money(entry.getRetailPrice() * entry.getAmount())
                    );
        }
    }
    
    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("buyer", buyer);
        args.put("seller", seller);
        args.put("entry", entry);
        return args;
    }
    
    public static BuyRejection deserialize(Map<String, Object> args)
    {
        return new BuyRejection(args);
    }
    
    public static BuyRejection valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    @Override
    public boolean checkIntegrity()
    {
        return true;
    }
}