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
package tbax.baxshops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public final class BaxShop implements ConfigurationSerializable
{
    public static final int ITEMS_PER_PAGE = 7;
    
    public long id = -1;
    public String owner;
    public ArrayList<Location> locations = new ArrayList<>();
    public ArrayList<BaxEntry> inventory = new ArrayList<>();
    // shop flags
    public boolean infinite = false;
    public boolean sellToShop = false;
    public boolean notify = true;
    public boolean buyRequests = false;
    public boolean sellRequests = true;
    
    public BaxShop()
    {
    }
    
    public BaxShop(Map<String, Object> args)
    {
        if (args.get("id") instanceof Integer) {
            id = (int)args.get("id");
        }
        else {
            id = (long)args.get("id");
        }
        if (args.containsKey("buyRequests")) {
            buyRequests = (boolean)args.get("buyRequests");
        }
        if (args.containsKey("infinite")) {
            infinite = (boolean)args.get("infinite");
        }
        if (args.containsKey("notify")) {
            notify = (boolean)args.get("notify");
        }
        if (args.containsKey("sellRequests")) {
            sellRequests = (boolean)args.get("sellRequests");
        }
        if (args.containsKey("sellToShop")) {
            sellToShop = (boolean)args.get("sellToShop");
        }
        owner = (String)args.get("owner");
        inventory = (ArrayList)args.get("inventory");
        locations = (ArrayList)args.get("locations");
        if (infinite) {
            for(BaxEntry entry : inventory) {
                entry.infinite = infinite;
            }
        }
    }
    
    public int getIndexOfEntry(BaxEntry entry)
    {
        for(int index = 0; index < inventory.size(); index++) {
            if (inventory.get(index).equals(entry)) {
                return index;
            }
        }
        return -1; // not found
    }
    
    public ArrayList<Location> getLocations()
    {
        if (locations == null) {
            locations = new ArrayList<>();
        }
        return locations;
    }
    
    private static boolean compareLoc(Location a, Location b)
    {
        return a.getBlockX() == b.getBlockX() &&
               a.getBlockY() == b.getBlockY() &&
               a.getBlockZ() == b.getBlockZ() &&
               a.getWorld().equals(b.getWorld());
    }
    
    public boolean hasLocation(Location loc)
    {
        for(Location l : locations) {
            if (compareLoc(l, loc)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean removeLocation(Location loc)
    {
        for(int i = 0; i < locations.size(); ++i) {
            if (compareLoc(locations.get(i), loc)) {
                locations.remove(i);
                return true;
            }
        }
        return false;
    }
    
    public void addLocation(Location loc)
    {
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
    public int getPages()
    {
        return ceil((double) inventory.size() / ITEMS_PER_PAGE);
    }

    /**
     * Gets the number of items in this shop's inventory.
     *
     * @return the number of items
     */
    public int getInventorySize()
    {
        return inventory.size();
    }

    /**
     * Gets the entry at the given index in this shop's inventory.
     *
     * @param index
     * @return the shop entry
     */
    public BaxEntry getEntryAt(int index)
    {
        return inventory.get(index);
    }

    /**
     * Add an item to this shop's inventory.
     * @param entry
     */
    public void addEntry(BaxEntry entry)
    {
        inventory.add(entry);
    }

    /**
     * Checks if this shop's inventory contains an item.
     *
     * @param stack the item to check for
     * @return whether the shop contains the item
     */
    public boolean containsItem(ItemStack stack)
    {
        return findEntry(stack) != null;
    }

    /**
     * Find an entry for an item in this shop's inventory.
     *
     * @param stack the item to find
     * @return the item's entry, or null
     */
    public BaxEntry findEntry(ItemStack stack)
    {
        for (BaxEntry e : inventory) {
            if (e.isItemEqual(stack)) {
                return e;
            }
        }
        return null;
    }
    
    public ItemStack toItem(List<String> sign)
    {
        ItemStack item = new ItemStack(Material.SIGN, 1);
        ArrayList<String> lore = new ArrayList<>();
        lore.addAll(sign);
        lore.add("ID: " + id);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(owner + "'s Shop");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    @Override
    public Map<String, Object> serialize()
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("owner", owner);
        if (infinite) {
            map.put("infinite", infinite);
        }
        if (!notify) {
            map.put("notify", notify);
        }
        if (sellToShop) {
            map.put("sellToShop", sellToShop);   
        }
        if (buyRequests) {
            map.put("buyRequests", buyRequests);
        }
        if (!sellRequests) {
            map.put("sellRequests", sellRequests);
        }
        map.put("inventory", inventory);
        map.put("locations", locations);
        return map;
    }
    
    public static BaxShop deserialize(Map<String, Object> args)
    {
        return new BaxShop(args);
    }
    
    public static BaxShop valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
