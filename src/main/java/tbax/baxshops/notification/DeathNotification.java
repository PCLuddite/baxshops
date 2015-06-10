package tbax.baxshops.notification;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

public class DeathNotification implements Notification {
    private static final long serialVersionUID = 1L;
    
    public double tax;
    public String person;
    
    public DeathNotification(String person, double tax) {
        this.person = person;
        this.tax = tax;
    }
    
    @Override
    public String getMessage(Player player) {
        if (player == null || !player.getName().equals(person)) {
            return String.format("%s was fined $%.2f for dying.", person, tax);
        }
        else {
            return String.format("§FYou were fined §a$%.2f§F for dying.", tax);
        }
    }

    public static final String TYPE_ID = "DeathNote";
    
    @Override
    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type", TYPE_ID);
        o.addProperty("person", person);
        o.addProperty("tax", tax);
        return o;
    }
    
    public DeathNotification() {
    }
    
    public static DeathNotification fromJson(JsonObject o) {
        DeathNotification death = new DeathNotification();
        death.tax = o.get("tax").getAsDouble();
        death.person = o.get("person").getAsString();
        return death;
    }

}
