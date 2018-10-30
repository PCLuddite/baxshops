/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
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
        for(String line : sign) {
            lore.add(ChatColor.BLUE + line);
        }
        lore.add(ChatColor.GRAY + "ID: " + id);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + owner + "'s shop");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Determines whether an item is a shop
     * @param item
     * @return 
     */
    public static boolean isShop(ItemStack item)
    {
        return item.getType() == Material.SIGN &&
               item.hasItemMeta() &&
               item.getItemMeta().hasLore() &&
               item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1).startsWith(ChatColor.GRAY + "ID: ");
    }
    
    /**
     * Converts an item to a BaxShop
     * Note: This should only be used after calling isShop()
     * @param item
     * @return 
     */
    public static BaxShop fromItem(ItemStack item)
    {
        long uid = Long.parseLong(item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1).substring((ChatColor.GRAY + "ID: ").length()));
        return Main.getState().getShop(uid);
    }
    
    /**
     * Extracts the sign text from the lore of a shop item
     * Note: This should only be used after calling isShop()
     * @param item
     * @return 
     */
    public static String[] extractSignText(ItemStack item)
    {
        List<String> lore = item.getItemMeta().getLore().subList(0, item.getItemMeta().getLore().size() - 1);
        String[] lines = new String[lore.size()];
        for(int i = 0; i < lines.length; ++i) {
            lines[i] = ChatColor.stripColor(lore.get(i));
        }
        return lines;
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
