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
public class SaleNotification implements Notification {
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
    public SaleNotification(BaxShop shop, BaxEntry entry, String seller) {
        this.shop = shop;
        this.entry = entry;
        this.seller = seller;
    }

    @Override
    public String getMessage(Player player) {
        return player == null || !player.getName().equals(seller) ?
                String.format("%s accepted %s's request to sell %d %s for $%.2f",
                                shop.owner, seller, entry.getAmount(), ItemNames.getItemName(entry),
                                entry.refundPrice * entry.getAmount()) :
                String.format("§1%s §faccepted your request to sell §e%d %s§F for §a$%.2f§F",
                                shop.owner, entry.getAmount(), ItemNames.getItemName(entry),
                                entry.refundPrice * entry.getAmount());
    }

    public static final String TYPE_ID = "SaleNote";

    @Override
    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type", TYPE_ID);
        o.addProperty("seller", seller);
        o.addProperty("shop", shop.uid);
        o.add("entry", entry.toJson());
        return o;
    }
    
    public SaleNotification() {
    }
    
    public static SaleNotification fromJson(JsonObject o) {
        SaleNotification note = new SaleNotification();
        note.seller = o.get("seller").getAsString();
        note.shop = Main.instance.state.getShop(o.get("shop").getAsInt());
        note.entry = new BaxEntry(o.get("entry").getAsJsonObject());
        return note;
    }
}