/*
 * Copyright (C) Timothy Baxendale
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package tbax.shops.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.bukkit.Location;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.serialization.internal.states.State_00200;
import tbax.shops.BaxShop;
import tbax.shops.notification.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class JsonState
{
    public static final String JSON_FILE_PATH = "shops.json";

    public HashMap<Location, Integer> locations;
    public HashMap<Integer, BaxShop> shops;
    public HashMap<String, ArrayDeque<Notification>> pending;
    private final Logger log;
    private final ShopPlugin main;

    public JsonState(ShopPlugin main)
    {
        this.locations = new HashMap<>();
        this.shops = new HashMap<>();
        this.pending = new HashMap<>();
        this.main = main;
        this.log = main.getLogger();
    }

    public JsonObject loadState()
    {
        File stateLocation = new File(main.getDataFolder(), JSON_FILE_PATH);
        if (!stateLocation.exists()) {
            return null;
        }
        try (JsonReader jr = new JsonReader(new FileReader(stateLocation))) {
            JsonParser p = new JsonParser();
            JsonElement element = p.parse(jr);
            if (element.isJsonObject()) {
                return element.getAsJsonObject();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadShops(State_00200 loader, JsonObject shopObject)
    {
        for (Map.Entry<String, JsonElement> entry : shopObject.entrySet()) {
            try {
                int uid = Integer.parseInt(entry.getKey());
                BaxShop shop = new BaxShop(loader, uid, entry.getValue().getAsJsonObject());
                shops.put(shop.uid, shop);
                for (final Location loc : shop.getLocations()) {
                    this.locations.put(loc, shop.uid);
                }
            }
            catch (NumberFormatException e) {
                log.warning("Invalid shop id '" + entry.getKey() + "'! Shop has been skipped. If you believe this is an error, try loading a backup.");
            }
        }
    }

    public void loadNotes(State_00200 loader, JsonObject noteObject)
    {
        for (Map.Entry<String, JsonElement> entry : noteObject.entrySet()) {
            ArrayDeque<Notification> notes = new ArrayDeque<>();
            if (entry.getValue().isJsonArray()) {
                for (JsonElement e : entry.getValue().getAsJsonArray()) {
                    if (e.isJsonObject()) {
                        Notification n = loadNote(loader, e.getAsJsonObject());
                        if (n == null) {
                            continue;
                        }
                        notes.add(n);
                    }
                    else {
                        log.warning("Invalid notification type. Skipping.");
                    }
                }
            }
            pending.put(entry.getKey(), notes);
        }
    }

    private Notification loadNote(State_00200 loader, JsonObject o)
    {
        String asString = o.get("type").getAsString();
        switch (asString) {
            case "BuyClaim": return new BuyClaim(loader, o);
            case "BuyNote": return new BuyNotification(loader, o);
            case "BuyReject": return new BuyRejection(loader, o);
            case "BuyRequest": return new BuyRequest(loader, o);
            case "DeathNote": return new DeathNotification(o);
            case "general": return new GeneralNotification(o);
            case "lolly": return new LollipopNotification(o);
            case "SaleNote": return new SaleNotification(loader, o);
            case "SaleNoteAuto": return new SaleNotificationAuto(loader, o);
            case "SaleReject": return new SaleRejection(loader, o);
            case "SellRequest": return new SellRequest(loader, o);
            default:
                log.warning("Unknown message type '" + o.get("type").getAsString() + "'. Skipped.");
                return null;
        }
    }
}
