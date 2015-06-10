package tbax.baxshops.notification;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Main;
import tbax.baxshops.serialization.ItemNames;

/**
 * A BuyNotification notifies a shop owner that someone bought an item
 * from him/her.
 */
public class BuyNotification implements Notification {
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
    public String buyer;

    /**
     * Constructs a new notification.
     * @param shop the shop to which the seller was selling
     * @param entry an entry for the item (note: not the one in the shop)
     * @param buyer the buyer of the item
     */
    public BuyNotification(BaxShop shop, BaxEntry entry, String buyer) {
        this.shop = shop;
        this.entry = entry;
        this.buyer = buyer;
    }
    
    @Override
    public String getMessage(Player player) {
        if (player == null || !player.getName().equals(shop.owner)) {
            return String.format("%s bought %d %s from %s for $%.2f",
                        buyer, entry.getAmount(), ItemNames.getItemName(entry),
                        shop.owner, entry.retailPrice * entry.getAmount());
        }
        else {
            return String.format("§1%s §fbought §e%d %s§F from you for §a$%.2f§F",
                            buyer, entry.getAmount(), ItemNames.getItemName(entry),
                            entry.retailPrice * entry.getAmount());
        }
    }

    public static final String TYPE_ID = "BuyNote";
    
    @Override
    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type", TYPE_ID);
        o.addProperty("buyer", buyer);
        o.addProperty("shop", shop.uid);
        o.add("entry", entry.toJson());
        return o;
    }
    
    public BuyNotification() {
    }
    
    public static BuyNotification fromJson(JsonObject o) {
        BuyNotification note = new BuyNotification();
        note.buyer = o.get("buyer").getAsString();
        note.shop = Main.instance.state.getShop(o.get("shop").getAsInt());
        note.entry = new BaxEntry(o.get("entry").getAsJsonObject());
        return note;
    }
}
