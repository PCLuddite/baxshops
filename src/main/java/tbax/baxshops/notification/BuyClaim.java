/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import java.util.HashMap;
import java.util.Map;
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
public final class BuyClaim extends Claimable
{
    /**
     *  The user who sold the item
     */
    public String seller;
    /**
     * The seller of the item
     */
    public String buyer;
    
    /**
     * Constructs a new notification.
     * @param seller the username of the seller
     * @param entry an entry for the item (note: not the one in the shop)
     * @param buyer the buyer of the item
     */
    public BuyClaim(String seller, BaxEntry entry, String buyer) 
    {
        this.seller = seller;
        this.entry = entry;
        this.buyer = buyer;
    }
    
    public BuyClaim(Map<String, Object> args)
    {
        this.buyer = (String)args.get("buyer");
        this.entry = (BaxEntry)args.get("entry");
        if (args.containsKey("shop")) {
            BaxShop shop = Main.getState().getShop((int)args.get("shop"));
            if (shop == null) {
                seller = Resources.ERROR_INLINE;
            }
            else {
                seller = shop.getOwner();
            }
        }
        else {
            seller = (String)args.get("seller");
        }
    }

    @Override
    public String getMessage(ShopCmdActor actor)
    {
        if (actor.getPlayer() == null || !actor.getPlayer().getName().equals(buyer)) {
            return String.format("%s accepted %s's request to buy %s for %s.",
                        Format.username(seller), 
                        Format.username2(buyer),
                        Format.itemname(entry.getAmount(), ItemNames.getName(entry)),
                        Format.money(entry.getRetailPrice() * entry.getAmount())
                    );
        }
        else {
            return String.format("%s accepted your request to buy %s for %s.",
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
    
    public static BuyClaim deserialize(Map<String, Object> args)
    {
        return new BuyClaim(args);
    }
    
    public static BuyClaim valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    @Override
    public boolean checkIntegrity()
    {
        return true;
    }
}