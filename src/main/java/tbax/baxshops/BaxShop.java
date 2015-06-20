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
package tbax.baxshops;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import tbax.shops.serialization.BlockLocation;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public class BaxShop {
    public static final int ITEMS_PER_PAGE = 7;
    
    public int uid = -1;
    public ArrayList<BaxEntry> inventory = new ArrayList<>();
    public String owner;
    public boolean infinite = false;
    public boolean sellToShop = false;
    public boolean notify = true;
    public boolean buyRequests = false;
    public boolean sellRequests = true;
    private ArrayList<Location> locations = new ArrayList<>();
    
    public BaxShop() {
    }
    
    public BaxShop(int uid, tbax.shops.BaxShop shop) {
        this.uid          = uid;
        this.owner        = shop.owner;
        this.infinite     = shop.isInfinite;
        this.sellRequests = (boolean)shop.getOption("sell_request");
        this.buyRequests  = (boolean)shop.getOption("buy_request");
        this.sellToShop   = (boolean)shop.getOption("sell_to_shop");
        for(tbax.shops.ShopEntry oldEntry : shop.inventory) {
            inventory.add(new BaxEntry(oldEntry));
        }
        locations.add(shop.location);
        if (shop.getOption("ref_list") instanceof ArrayList) {
            @SuppressWarnings("unchecked")
            ArrayList<BlockLocation> refLocs = (ArrayList<BlockLocation>)shop.getOption("ref_list");
            for(BlockLocation oldref : refLocs) {
                locations.add(oldref.toLocation());
            }
        }
    }
    
    public BaxShop(int uid, JsonObject o) {
        this.uid         = uid;
        this.owner       = o.get("owner").getAsString();
        if (o.has("infinite")) {
            this.infinite = o.get("infinite").getAsBoolean();
        }
        if (o.has("sellToShop")) {
            this.sellToShop = o.get("sellToShop").getAsBoolean();
        }
        if (o.has("buyRequests")) {
            this.buyRequests = o.get("buyRequests").getAsBoolean();
        }
        if (o.has("sellRequests")) {
            this.sellRequests = o.get("sellRequests").getAsBoolean();
        }
        if (o.has("notify")) {
            this.notify = o.get("notify").getAsBoolean();
        }
        loadLocations(o.get("locations").getAsJsonArray());
        loadEntries(o.get("entries").getAsJsonArray());
    }
    
    private void loadLocations(JsonArray a) {
        for(int i = 0; i < a.size(); ++i) {
            Location loc = getLocFromJson(a.get(i).getAsJsonObject());
            locations.add(loc);
        }
    }
    
    private void loadEntries(JsonArray a) {
        for(int i = 0; i < a.size(); ++i) {
            BaxEntry entry = new BaxEntry(a.get(i).getAsJsonObject());
            inventory.add(entry);
        }
    }
    
    private static Location getLocFromJson(JsonObject o) {
        return new Location(
                Main.instance.getServer().getWorld(o.get("world").getAsString()),
                o.get("x").getAsInt(),
                o.get("y").getAsInt(),
                o.get("z").getAsInt()
        );
    }
    
    public int getIndexOfEntry(Material material, int damage) {
        for(int index = 0; index < inventory.size(); index++) {
            if (inventory.get(index).getType() == material && inventory.get(index).getDurability() == damage) {
                return index;
            }
        }
        return -1;
    }
    
    public ArrayList<Location> getLocations() {
        return locations;
    }
    
    private static boolean compareLoc(Location a, Location b) {
        return a.getBlockX() == b.getBlockX() &&
               a.getBlockY() == b.getBlockY() &&
               a.getBlockZ() == b.getBlockZ() &&
               a.getWorld() == b.getWorld();
    }
    
    public boolean hasLocation(Location loc) {
        for(Location l : locations) {
            if (compareLoc(l, loc)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean removeLocation(Location loc) {
        for(int i = 0; i < locations.size(); ++i) {
            if (compareLoc(locations.get(i), loc)) {
                locations.remove(i);
                return true;
            }
        }
        return false;
    }
    
    public void addLocation(Location loc) {
        locations.add(loc);
    }
    
    private int ceil(double x) {
        return (int) Math.ceil(x);
    }

    /**
     * Gets the number of pages in this shop's inventory.
     *
     * @return the number of pages
     */
    public int getPages() {
        return ceil((double) inventory.size() / ITEMS_PER_PAGE);
    }

    /**
     * Gets the number of items in this shop's inventory.
     *
     * @return the number of items
     */
    public int getInventorySize() {
        return inventory.size();
    }

    /**
     * Gets the entry at the given index in this shop's inventory.
     *
     * @param index
     * @return the shop entry
     */
    public BaxEntry getEntryAt(int index) {
        return inventory.get(index);
    }

    /**
     * Add an item to this shop's inventory.
     * @param entry
     */
    public void addEntry(BaxEntry entry) {
        inventory.add(entry);
    }

    /**
     * Checks if this shop's inventory contains an item.
     *
     * @param stack the item to check for
     * @return whether the shop contains the item
     */
    public boolean containsItem(ItemStack stack) {
        for (BaxEntry e : inventory) {
            if (e.getType() == stack.getType()
                    && e.getDurability() == stack.getDurability()) {
                for (Map.Entry<Enchantment, Integer> entry : stack.getEnchantments().entrySet()) {
                    Integer level = e.getEnchantments().get(entry.getKey());
                    if (!Objects.equals(level, entry.getValue())) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if this shop's inventory contains an item.
     *
     * @param id the item's ID
     * @param damage the item's damage value (durability)
     * @return whether the shop contains the item
     */
    public boolean containsItem(Material id, int damage) {
        for (BaxEntry e : inventory) {
            if (e.getType() == id
                    && e.getDurability() == damage) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find an entry for an item in this shop's inventory.
     *
     * @param stack the item to find
     * @return the item's entry, or null
     */
    public BaxEntry findEntry(ItemStack stack) {
        for (BaxEntry e : inventory) {
            if (e.getType() == stack.getType()
                    && e.getDurability() == stack.getDurability()) {
                for (Map.Entry<Enchantment, Integer> entry : stack.getEnchantments().entrySet()) {
                    Integer level = e.getEnchantments().get(entry.getKey());
                    if (!Objects.equals(level, entry.getValue())) {
                        return null;
                    }
                }
                return e;
            }
        }
        return null;
    }

    /**
     * Find an entry for an item in this shop's inventory.
     *
     * @param material the item's ID
     * @param damage the item's damage value (durability)
     * @return the item's entry, or null
     */
    public BaxEntry findEntry(Material material, int damage) {
        for (BaxEntry e : inventory) {
            if (e.getType() == material && e.getDurability() == damage) {
                return e;
            }
        }
        return null;
    }

    private static void addIfTrue(JsonObject o, String key, boolean value) {
	if (value) {
            o.addProperty(key, value);
	}
    }
    
    private static void addIfFalse(JsonObject o, String key, boolean value) {
	if (!value) {
            o.addProperty(key, value);
	}
    }

    private static JsonElement toJson(Location loc) {
        JsonObject o = new JsonObject();
        o.addProperty("world", loc.getWorld().getName());
        o.addProperty("x", loc.getBlockX());
        o.addProperty("y", loc.getBlockY());
        o.addProperty("z", loc.getBlockZ());
        return o;
    }
    
    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("owner", owner);
        // default false
        addIfTrue(o, "infinite", infinite);
        addIfTrue(o, "sellToShop", sellToShop);
        addIfTrue(o, "buyRequests", buyRequests);
        // default true
        addIfFalse(o, "sellRequests", sellRequests);
        addIfFalse(o, "notify", notify);
        
        JsonArray aLocs = new JsonArray();
        for(Location loc : locations) {
            aLocs.add(toJson(loc));
        }
        o.add("locations", aLocs);
        
        JsonArray aItems = new JsonArray();
        for(BaxEntry e : inventory) {
            aItems.add(e.toJson());
        }
        o.add("entries", aItems);
        
        return o;
    }
}
