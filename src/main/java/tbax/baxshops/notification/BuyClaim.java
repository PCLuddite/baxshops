/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * @author Timothy
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
            return String.format("%s accepted %s's request to buy %d %s for $%.2f",
                                shop.owner, buyer, entry.getAmount(), ItemNames.getItemName(entry),
                                entry.retailPrice * entry.getAmount());
        }
        else {
            return String.format("%s accepted your request to buy §e%d %s§F for §a$%.2f",
                                shop.owner, entry.getAmount(), ItemNames.getItemName(entry),
                                entry.retailPrice * entry.getAmount());
        }
    }

    public static final String TYPE_ID = "BuyClaim";
    
    @Override
    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type", TYPE_ID);
        o.addProperty("buyer", buyer);
        o.addProperty("shop", shop.uid);
        o.add("entry", entry.toJson());
        return o;
    }
    
    public BuyClaim() {
    }
    
    public static BuyClaim fromJson(JsonObject o) {
        BuyClaim claim = new BuyClaim();
        claim.buyer = o.get("buyer").getAsString();
        claim.shop = Main.instance.state.getShop(o.get("shop").getAsInt());
        claim.entry = new BaxEntry(o.get("entry").getAsJsonObject());
        return claim;
    }
}
