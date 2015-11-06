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
import org.bukkit.entity.Player;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.serialization.ItemNames;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public class BuyClaim implements Claimable {
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
    public BuyClaim(BaxShop shop, BaxEntry entry, String buyer) {
        this.shop = shop;
        this.entry = entry;
        this.buyer = buyer;
    }
    
    @Override
    public boolean claim(Player player) {
        if (Main.giveToPlayer(player, entry.toItemStack())) {
            if (entry.getAmount() == 1) {
                player.sendMessage("§fThe item have been added to your inventory.");
            }
            else {
                player.sendMessage("§fThe items has been added to your inventory.");
            }
            return true;
        }
        else {
            Main.sendError(player, Resources.NO_ROOM);
            return false;
        }
    }
    
    public String getMessage(Player player) {
        if (player == null || !player.getName().equals(buyer)) {
            return String.format("§5%s §faccepted %s's request to buy §e%d %s§f for $%.2f",
                                shop.owner, buyer, entry.getAmount(), ItemNames.getItemName(entry),
                                entry.retailPrice * entry.getAmount());
        }
        else {
            return String.format("§1%s §faccepted your request to buy §e%d %s§F for §a$%.2f",
                                shop.owner, entry.getAmount(), ItemNames.getItemName(entry),
                                entry.retailPrice * entry.getAmount());
        }
    }

    public static final String TYPE_ID = "BuyClaim";
    
    @Override
    public JsonElement toJson(double version) {
        JsonObject o = new JsonObject();
        o.addProperty("type", TYPE_ID);
        o.addProperty("buyer", buyer);
        o.addProperty("shop", shop.uid);
        o.add("entry", entry.toJson(version));
        return o;
    }
    
    public BuyClaim() {
    }
    
    public static BuyClaim fromJson(double version, JsonObject o) {
        BuyClaim claim = new BuyClaim();
        claim.buyer = o.get("buyer").getAsString();
        claim.shop = Main.instance.state.getShop(o.get("shop").getAsInt());
        claim.entry = new BaxEntry(version, o.get("entry").getAsJsonObject());
        return claim;
    }
}
