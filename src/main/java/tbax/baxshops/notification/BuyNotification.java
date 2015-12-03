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

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.serialization.ItemNames;

/**
 * A BuyNotification notifies a shop owner that someone bought an item
 * from him/her.
 */
public final class BuyNotification implements Notification
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
     * The seller of the item
     */
    public String buyer;

    public BuyNotification(Map<String, Object> args)
    {
        buyer = (String)args.get("buyer");
        entry = (BaxEntry)args.get("entry");
        if (args.containsKey("seller")) {
            seller = (String)args.get("seller");
        }
        else if (args.containsKey("shop")) {
            BaxShop shop = Main.getState().getShop((int)args.get("shop"));
            if (shop == null) {
                seller = Resources.ERROR_INLINE;
            }
            else {
                seller = shop.owner;
            }
        }
    }
    
    /**
     * Constructs a new notification.
     * @param seller the username of the seller
     * @param entry an entry for the item (note: not the one in the shop)
     * @param buyer the buyer of the item
     */
    public BuyNotification(String buyer, String seller, BaxEntry entry)
    {
        this.seller = seller;
        this.entry = entry;
        this.buyer = buyer;
    }
    
    @Override
    public String getMessage(Player player) 
    {
        if (player == null || !player.getName().equals(seller)) {
            return String.format("%s bought %s from %s for %s.",
                        Format.username(buyer),
                        Format.itemname(entry.getAmount(), ItemNames.getName(entry)),
                        Format.username2(seller),
                        Format.money(entry.retailPrice * entry.getAmount())
                    );
        }
        else {
            return String.format("%s bought %s from you for %s.",
                        Format.username(buyer),
                        Format.itemname(entry.getAmount(), ItemNames.getName(entry)),
                        Format.money(entry.retailPrice * entry.getAmount())
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
    
    public static BuyNotification deserialize(Map<String, Object> args)
    {
        return new BuyNotification(args);
    }
    
    public static BuyNotification valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    @Override
    public boolean checkIntegrity()
    {
        return true;
    }
}