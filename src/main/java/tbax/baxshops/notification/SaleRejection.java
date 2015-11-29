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
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.serialization.BaxEntryDeserializer;
import tbax.baxshops.serialization.BaxEntrySerializer;
import tbax.baxshops.serialization.ItemNames;

/**
 * A SaleRejection notifies a seller that his/her offer was rejected.
 */
public final class SaleRejection implements ConfigurationSerializable, Claimable
{
    private static final long serialVersionUID = 1L;
    /**
     * An entry for the offered item
     */
    public BaxEntry entry;
    /**
     * The shop to which the item is being sold
     */
    public BaxShop shop;
    /**
     * The seller of the item
     */
    public String seller;

    /**
     * Constructs a new notification.
     * @param shop the shop to which the seller was selling
     * @param entry an entry for the item (note: not the one in the shop)
     * @param seller the seller of the item
     */
    public SaleRejection(BaxShop shop, BaxEntry entry, String seller)
    {
        this.shop = shop;
        this.entry = entry;
        this.seller = seller;
    }
    
    public SaleRejection(Map<String, Object> args)
    {
        this.seller = (String)args.get("seller");
        this.entry = (BaxEntry)args.get("entry");
        this.shop = Main.instance.state.getShop((int)args.get("shop"));
    }

    @Override
    public String getMessage(Player player)
    {
        if (player == null || !player.getName().equals(seller)) {
            return String.format("%s rejected %s's request to sell %s for %s.",
                Format.username(shop.owner),
                Format.username2(seller),
                Format.itemname(entry.getAmount(), ItemNames.getItemName(entry)),
                Format.money(entry.refundPrice * entry.getAmount())
            );
        }
        else {
            return String.format("%s rejected your request to sell %s for %s.",
                Format.username(shop.owner),
                Format.itemname(entry.getAmount(), ItemNames.getItemName(entry)),
                Format.money(entry.refundPrice * entry.getAmount())
            );
        }
    }

    @Override
    public boolean claim(Player player)
    {
        ItemStack item = entry.toItemStack();
        if (Main.inventoryFitsItem(player, item)){
            player.getInventory().addItem(item);
            return true;
        }
        else {
            Main.sendError(player, Resources.NO_ROOM);
            return false;
        }
    }

    @Override
    public boolean checkIntegrity()
    {
        return shop != null;
    }

    public static final String TYPE_ID = "SaleReject";
    
    @Override
    public JsonElement toJson(double version)
    {
        JsonObject o = new JsonObject();
        o.addProperty("type", "SaleReject");
        o.addProperty("seller", seller);
        o.addProperty("shop", shop.uid);
        o.add("entry", BaxEntrySerializer.serialize(version, entry));
        return o;
    }
    
    public SaleRejection()
    {
    }
    
    public static SaleRejection fromJson(double version, JsonObject o)
    {
        SaleRejection claim = new SaleRejection();
        claim.seller = o.get("seller").getAsString();
        claim.shop = Main.instance.state.getShop(o.get("shop").getAsInt());
        claim.entry = BaxEntryDeserializer.deserialize(version, o.get("entry").getAsJsonObject());
        return claim;
    }
    
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("seller", seller);
        args.put("shop", shop.uid);
        args.put("entry", entry);
        return args;
    }
    
    public static SaleRejection deserialize(Map<String, Object> args)
    {
        return new SaleRejection(args);
    }
    
    public static SaleRejection valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
