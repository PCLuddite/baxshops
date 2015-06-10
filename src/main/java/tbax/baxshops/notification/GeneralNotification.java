/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tbax.baxshops.notification;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

/**
 *
 * @author Timothy
 */
public class GeneralNotification implements Notification {
    
    public String message;
    
    public GeneralNotification(String msg) {
        message = msg;
    }
    
    public String getMessage(Player player) {
        return message;
    }

    public static final String TYPE_ID = "general";
    
    @Override
    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type", TYPE_ID);
        o.addProperty("msg", message);
        return o;
    }
    
    public GeneralNotification() {
    }
    
    public static GeneralNotification fromJson(JsonObject o) {
        GeneralNotification general = new GeneralNotification();
        general.message = o.get("msg").getAsString();
        return general;
    }
}
