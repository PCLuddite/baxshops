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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.serialization.BaxEntryDeserializer;
import tbax.baxshops.serialization.BaxEntrySerializer;
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
     * The shop to which the item is being sold
     */
    public BaxShop shop;
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
    public BuyClaim(BaxShop shop, BaxEntry entry, String buyer) 
    {
        this.shop = shop;
        this.entry = entry;
        this.buyer = buyer;
    }
    
    public BuyClaim() 
    {
    }
    
    public BuyClaim(Map<String, Object> args)
    {
        this.buyer = (String)args.get("buyer");
        this.entry = (BaxEntry)args.get("entry");
        this.shop = Main.instance.state.getShop((int)args.get("shop"));
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
                        Format.username(shop.owner), 
                        Format.username2(buyer),
                        Format.itemname(entry.getAmount(), ItemNames.getItemName(entry)),
                        Format.money(entry.retailPrice * entry.getAmount())
                    );
        }
        else {
            return String.format("%s accepted your request to buy %s for %s.",
                    Format.username(shop.owner),
                    Format.itemname(entry.getAmount(), ItemNames.getItemName(entry)),
                    Format.money(entry.retailPrice * entry.getAmount())
            );
        }
    }

    public static final String TYPE_ID = "BuyClaim";
    
    @Override
    public JsonElement toJson(double version)
    {
        JsonObject o = new JsonObject();
        o.addProperty("type", TYPE_ID);
        o.addProperty("buyer", buyer);
        o.addProperty("shop", shop.uid);
        o.add("entry", BaxEntrySerializer.serialize(version, entry));
        return o;
    }
    
    public static BuyClaim fromJson(double version, JsonObject o)
    {
        BuyClaim claim = new BuyClaim();
        claim.buyer = o.get("buyer").getAsString();
        claim.shop = Main.instance.state.getShop(o.get("shop").getAsInt());
        claim.entry = BaxEntryDeserializer.deserialize(version, o);
        return claim;
    }

    @Override
    public boolean checkIntegrity()
    {
        return shop != null;
    }
    
    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("buyer", buyer);
        args.put("shop", shop.uid);
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
}
