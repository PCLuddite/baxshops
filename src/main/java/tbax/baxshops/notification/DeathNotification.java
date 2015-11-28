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
import tbax.baxshops.Format;

public final class DeathNotification implements ConfigurationSerializable, Notification
{
    private static final long serialVersionUID = 1L;
    
    public double tax;
    public String person;
    
    public DeathNotification(String person, double tax)
    {
        this.person = person;
        this.tax = tax;
    }

    public DeathNotification(Map<String, Object> args)
    {
        this.person = (String)args.get("person");
        this.tax = (double)args.get("tax");
    }
    
    @Override
    public String getMessage(Player player)
    {
        if (player == null || !player.getName().equals(person)) {
            return String.format("%s was fined %s for dying.", Format.username(person), Format.money(tax));
        }
        else {
            return String.format("You were fined %s for dying.", Format.money(tax));
        }
    }

    public static final String TYPE_ID = "DeathNote";
    
    @Override
    public JsonElement toJson(double version)
    {
        JsonObject o = new JsonObject();
        o.addProperty("type", TYPE_ID);
        o.addProperty("person", person);
        o.addProperty("tax", tax);
        return o;
    }
    
    public DeathNotification()
    {
    }
    
    public static DeathNotification fromJson(double version, JsonObject o)
    {
        DeathNotification death = new DeathNotification();
        death.tax = o.get("tax").getAsDouble();
        death.person = o.get("person").getAsString();
        return death;
    }
    
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("person", person);
        args.put("tax", tax);
        return args;
    }
    
    public static DeathNotification deserialize(Map<String, Object> args)
    {
        return new DeathNotification(args);
    }
    
    public static DeathNotification valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
