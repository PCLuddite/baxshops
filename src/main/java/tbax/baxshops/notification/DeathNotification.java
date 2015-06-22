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
            return String.format("§5%s§f was fined §a$%.2f§f for dying.", person, tax);
        }
        else {
            return String.format("§fYou were fined §a$%.2f§f for dying.", tax);
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
