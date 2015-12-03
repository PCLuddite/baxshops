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
public class SaleNotificationAuto implements Notification, ConfigurationSerializable 
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

    public SaleNotificationAuto(Map<String, Object> args)
    {
        this.seller = (String)args.get("seller");
        this.entry = (BaxEntry)args.get("entry");
        if (args.containsKey("shop")) {
            BaxShop shop = Main.instance.state.getShop((int)args.get("shop"));
            if (shop == null) {
                buyer = Resources.ERROR_INLINE;
            }
            else {
                buyer = shop.owner;
            }
        }
    }

    /**
     * Constructs a new notification.
     * @param buyer the buyer of the item
     * @param entry an entry for the item (note: not the one in the shop)
     * @param seller the seller of the item
     */
    public SaleNotificationAuto(String buyer, String seller, BaxEntry entry) 
    {
        this.buyer = buyer;
        this.entry = entry;
        this.seller = seller;
    }

    @Override
    public String getMessage(Player player) 
    {
        return getMessage(player == null ? null : player.getName(), buyer, seller, entry);
    }

    public static String getMessage(String player, String buyer, String seller, BaxEntry entry)
    {
        if (player == null || !player.equals(buyer)) {
            return String.format("%s sold %s to %s for %s.",
                        Format.username(seller),
                        Format.itemname(entry.getAmount(), ItemNames.getName(entry)),
                        Format.username2(buyer),
                        Format.money(entry.refundPrice * entry.getAmount())
                    );
        }
        else {
            return String.format("%s sold you %s for %s.",
                        Format.username(seller),
                        Format.itemname(entry.getAmount(), ItemNames.getName(entry)),
                        Format.money(entry.refundPrice * entry.getAmount())
                    );
        }
    }
    
    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("seller", seller);
        args.put("buyer", buyer);
        args.put("entry", entry);
        return args;
    }
    
    public static SaleNotificationAuto deserialize(Map<String, Object> args)
    {
        return new SaleNotificationAuto(args);
    }
    
    public static SaleNotificationAuto valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    @Override
    public boolean checkIntegrity()
    {
        return true;
    }
}