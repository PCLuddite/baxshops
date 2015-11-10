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
import java.util.Map;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
    public ArrayList<Location> locations = new ArrayList<>();
    
    public BaxShop() {
    }
    
    public int getIndexOfEntry(BaxEntry entry) {
        for(int index = 0; index < inventory.size(); index++) {
            if (inventory.get(index).equals(entry)) {
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
        return findEntry(stack) != null;
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
            if (e.getType() == id && e.getDurability() == damage) {
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
            if (e.getType() == stack.getType() && e.getDurability() == stack.getDurability()) {
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
}
