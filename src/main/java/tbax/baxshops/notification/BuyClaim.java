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
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public final class BuyClaim implements ConfigurationSerializable, Claimable 
{
    /**
     * An entry for the purchased item
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
            BaxShop shop = Main.instance.state.getShop((int)args.get("shop"));
            if (shop == null) {
                seller = Resources.ERROR_INLINE;
            }
            else {
                seller = shop.owner;
            }
        }
        else {
            seller = (String)args.get("seller");
        }
    }
    
    @Override
    public boolean claim(Player player)
    {
        if (Main.tryGiveItem(player, entry.toItemStack())) {
            if (entry.getAmount() == 1) {
                player.sendMessage("The item have been added to your inventory.");
            }
            else {
                player.sendMessage("The items has been added to your inventory.");
            }
            return true;
        }
        else {
            Main.sendError(player, Resources.NO_ROOM);
            return false;
        }
    }
    
    public String getMessage(Player player)
    {
        if (player == null || !player.getName().equals(buyer)) {
            return String.format("%s accepted %s's request to buy %s for %s.",
                        Format.username(seller), 
                        Format.username2(buyer),
                        Format.itemname(entry.getAmount(), ItemNames.getItemName(entry)),
                        Format.money(entry.retailPrice * entry.getAmount())
                    );
        }
        else {
            return String.format("%s accepted your request to buy %s for %s.",
                    Format.username(seller),
                    Format.itemname(entry.getAmount(), ItemNames.getItemName(entry)),
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