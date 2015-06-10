package tbax.baxshops.notification;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.bukkit.entity.Player;

/**
 * A LollipopNotification notifies a player that someone sent him/her a
 * lollipop.
 */
public class LollipopNotification implements Notification {
    private static final long serialVersionUID = 1L;

    public static final double DEFAULT_TASTINESS = 40;
    public static final Map<Double, String> adjectives = new LinkedHashMap<>();
    static {
        adjectives.put(0.0, "a disgusting");
        adjectives.put(10.0, "a bad");
        adjectives.put(20.0, "an icky");
        adjectives.put(30.0, "a bland");
        adjectives.put(40.0, "a");
        adjectives.put(50.0, "an OK");
        adjectives.put(55.0, "a better-than-average");
        adjectives.put(60.0, "a good");
        adjectives.put(70.0, "a great");
        adjectives.put(80.0, "a tasty");
        adjectives.put(90.0, "a delicious");
        adjectives.put(99.0, "a wonderful");
    }

    public String sender;
    public double tastiness;

    public LollipopNotification(String sender, double tastiness) {
        this.sender = sender;
        this.tastiness = tastiness < 0 ? 0 : tastiness > 100 ? 100 : tastiness;
    }

    @Override
    public String getMessage(Player player) {
        String adjective = null;
        for (Entry<Double, String> entry : adjectives.entrySet()) {
                if (tastiness >= entry.getKey()) {
                        adjective = entry.getValue();
                }
        }
        return sender + " sent you " + adjective + " lollipop";
    }
        
    public static final String TYPE_ID = "lolly";

    @Override
    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type", TYPE_ID);
        o.addProperty("sender", sender);
        o.addProperty("tastiness", tastiness);
        return o;
    }

    public LollipopNotification() {
    }
    
    public static LollipopNotification fromJson(JsonObject o) {
        LollipopNotification lolly = new LollipopNotification();
        lolly.sender = o.get("sender").getAsString();
        lolly.tastiness = o.get("tastiness").getAsInt();
        return lolly;
    }
}
