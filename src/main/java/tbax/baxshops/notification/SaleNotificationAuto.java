/* 
 * The MIT License
 *
 * Copyright © 2015 Timothy Baxendale (pcluddite@hotmail.com) and 
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
import tbax.baxshops.serialization.ItemNames;

/**
 * A SaleNotification notifies a player that his/her sale of an
 * item was successful.
 */
public class SaleNotificationAuto implements Claimable {
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
    public SaleNotificationAuto(BaxShop shop, BaxEntry entry, String seller) {
        this.shop = shop;
        this.entry = entry;
        this.seller = seller;
    }

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
            return false;
        }
    }

    @Override
    public String getMessage(Player player) {
        return getMessage(player == null ? null : player.getName(), shop, entry, seller);

    }

    public static String getMessage(String buyer, BaxShop shop, BaxEntry entry, String seller) {
        if (buyer == null || !buyer.equals(shop.owner)) {
            return String.format("§5%s §fsold %s §e%d %s§f for §a$%.2f§f",
                        seller, shop.owner, entry.getAmount(), ItemNames.getItemName(entry),
                        entry.refundPrice * entry.getAmount());
        }
        else {
            return String.format("§1%s §fsold you §e%d %s§f for §a$%.2f§f",
                        seller, entry.getAmount(), ItemNames.getItemName(entry),
                        entry.refundPrice * entry.getAmount());
        }
    }
    
    public static final String TYPE_ID = "SaleNoteAuto";
    
    @Override
    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type", TYPE_ID);
        o.addProperty("seller", seller);
        o.addProperty("shop", shop.uid);
        o.add("entry", entry.toJson());
        return o;
    }
    
    public SaleNotificationAuto() {
    }
    
    public static SaleNotificationAuto fromJson(JsonObject o) {
        SaleNotificationAuto note = new SaleNotificationAuto();
        note.seller = o.get("seller").getAsString();
        note.shop = Main.instance.state.getShop(o.get("shop").getAsInt());
        note.entry = new BaxEntry(o.get("entry").getAsJsonObject());
        return note;
    }
}
