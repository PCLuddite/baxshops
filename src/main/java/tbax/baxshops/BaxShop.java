/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.ItemNames;
import tbax.baxshops.serialization.SavedData;

import java.util.*;

public final class BaxShop implements ConfigurationSerializable, Iterable<BaxEntry>
{
    public static final int ITEMS_PER_PAGE = 7;
    
    private UUID id;
    private UUID ownerId;
    private final ArrayList<Location> locations = new ArrayList<>();
    private final ArrayList<BaxEntry> inventory = new ArrayList<>();

    private long flags = BaxShopFlag.NOTIFY | BaxShopFlag.SELL_REQUESTS;

    public BaxShop()
    {
    }
    
    public BaxShop(Map<String, Object> args)
    {
        id = UUID.fromString((String)args.get("id"));
        ownerId = UUID.fromString((String)args.get("owner"));
        flags = (long)args.get("flags");
        inventory.addAll((ArrayList)args.get("inventory"));
        locations.addAll((ArrayList)args.get("locations"));
        if (hasFlagInfinite()) {
            for(BaxEntry entry : inventory) {
                entry.setInfinite(hasFlagInfinite());
            }
        }
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID newId)
    {
        id = newId;
    }

    public OfflinePlayer getOwner()
    {
        return Bukkit.getOfflinePlayer(ownerId);
    }

    public void setOwner(OfflinePlayer newOwner)
    {
        ownerId = newOwner.getUniqueId();
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
    
    public List<Location> getLocations()
    {
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

    public BaxEntry getEntry(String arg) throws PrematureAbortException
    {
        try {
            return getEntryAt(Integer.parseInt(arg) - 1);
        }
        catch (NumberFormatException e) {
            return ItemNames.getItemFromAlias(arg, this);
        }
        catch (IndexOutOfBoundsException e) {
            throw new CommandErrorException(e, Resources.NOT_FOUND_SHOPITEM);
        }
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
     * @return the item's entry, or null if not found
     */
    public BaxEntry findEntry(ItemStack stack)
    {
        for (BaxEntry e : inventory) {
            if (e.isSimilar(stack)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Find an entry for an item in this shop's inventory.
     *
     * @param entry the item to find
     * @return the item's entry, or null if not found
     */
    public BaxEntry findEntry(BaxEntry entry)
    {
        for (BaxEntry e : inventory) {
            if (e.isSimilar(entry)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Creates a sign item for a given shop location
     * @param index the index of the shop location
     * @return the ItemStack containing the sign
     */
    public ItemStack toItem(int index)
    {
        return toItem(locations.get(index));
    }

    /**
     * Creates a sign item for a given shop location
     * @param loc the shop location to pull sign text
     * @return the ItemStack containing the sign
     */
    public ItemStack toItem(Location loc)
    {
        ItemStack item = new ItemStack(Material.SIGN, 1);
        ArrayList<String> lore = new ArrayList<>();
        for(String line : getSignText(loc)) {
            lore.add(ChatColor.BLUE + line);
        }
        lore.add(ChatColor.GRAY + "ID: " + id);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + getOwner().getName() + "'s shop");
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
        UUID uid = UUID.fromString(item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1).substring((ChatColor.GRAY + "ID: ").length()));
        return SavedData.getShop(uid);
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

    public void setFlagBuyRequests(boolean value)
    {
        flags = BaxShopFlag.setFlag(flags, BaxShopFlag.BUY_REQUESTS, value);
    }

    public boolean hasFlagBuyRequests()
    {
        return BaxShopFlag.hasFlag(flags, BaxShopFlag.BUY_REQUESTS);
    }

    public void setFlagSellRequests(boolean value)
    {
        flags = BaxShopFlag.setFlag(flags, BaxShopFlag.SELL_REQUESTS, value);
    }

    public boolean hasFlagSellRequests()
    {
        return BaxShopFlag.hasFlag(flags, BaxShopFlag.SELL_REQUESTS);
    }

    public void setFlagNotify(boolean value)
    {
        flags = BaxShopFlag.setFlag(flags, BaxShopFlag.NOTIFY, value);
    }

    public boolean hasFlagNotify()
    {
        return BaxShopFlag.hasFlag(flags, BaxShopFlag.NOTIFY);
    }

    public void setFlagSellToShop(boolean value)
    {
        flags = BaxShopFlag.setFlag(flags, BaxShopFlag.SELL_TO_SHOP, value);
    }

    public boolean hasFlagSellToShop()
    {
        return BaxShopFlag.hasFlag(flags, BaxShopFlag.SELL_TO_SHOP);
    }

    public void setFlagInfinite(boolean value)
    {
        flags = BaxShopFlag.setFlag(flags, BaxShopFlag.INFINITE, value);
    }

    public boolean hasFlagInfinite()
    {
        return BaxShopFlag.hasFlag(flags, BaxShopFlag.INFINITE);
    }

    public String getSignTextString(int index)
    {
        return getSignTextString(locations.get(index));
    }

    public String getSignTextString(Location loc)
    {
        try {
            Sign sign = (Sign)loc.getBlock().getState();
            StringBuilder ret = new StringBuilder();
            for(int line = 0; line < 4; line++) {
                if (!sign.getLine(line).isEmpty()) {
                    if (ret.length() > 0 && ret.charAt(ret.length() - 1) != '|') {
                        ret.append("|");
                    }
                    ret.append(sign.getLine(line));
                }
            }
            if (ret.length() == 0) {
                return ChatColor.RED + "<NO TEXT>";
            }
            else {
                return ChatColor.GREEN.toString() + (ret.length() > 15 ? ret.toString().substring(0,14) : ret);
            }
        }
        catch(Exception ex){
            return ChatColor.RED + Resources.ERROR_INLINE;
        }
    }

    public String[] getSignText(int index)
    {
        return getSignText(locations.get(index));
    }

    public String[] getSignText(Location loc)
    {
        try {
            Sign sign = (Sign) loc.getBlock().getState();
            String[] allLines = sign.getLines();
            int emptylines = 0;
            for(int i = allLines.length - 1; i >= 0; --i) {
                if (allLines[i].isEmpty()) {
                    ++emptylines;
                }
                else {
                    break;
                }
            }
            if (emptylines == allLines.length) {
                return new String[0];
            }
            int start = 0, end = allLines.length - 1;

            while(allLines[start].isEmpty())
                ++start;
            while(allLines[end].isEmpty())
                --end;

            return Arrays.copyOfRange(allLines, start, end);
        } catch (ClassCastException e) {
            return new String[0];
        }
    }

    @Override
    public Map<String, Object> serialize()
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id.toString());
        map.put("owner", ownerId.toString());
        map.put("flag", flags);
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

    @Override
    public Iterator<BaxEntry> iterator()
    {
        return inventory.iterator();
    }

    public Block buildShopSign(Location loc, String[] signLines) throws PrematureAbortException
    {
        Location locUnder = loc;
        locUnder.setY(locUnder.getY() - 1);

        Block b = loc.getWorld().getBlockAt(loc);
        Block blockUnder = locUnder.getWorld().getBlockAt(locUnder);
        if (blockUnder.getType() == Material.AIR || blockUnder.getType() == Material.TNT){
            throw new CommandErrorException("Sign does not have a block to place it on");
        }

        byte angle = (byte) ((((int) loc.getYaw() + 225) / 90) << 2);

        b.setType(Material.SIGN_POST);
        try {
            b.setData(angle, false);
        }
        catch(Exception e) {
        }
        if (!b.getType().equals(Material.SIGN)) {
            b.setType(Material.SIGN_POST);
            if (!b.getType().equals(Material.SIGN) && !b.getType().equals(Material.SIGN_POST)) {
                throw new CommandErrorException(String.format("Unable to place sign! Block type is %s.", b.getType().toString()));
            }
        }

        addLocation(loc);

        Sign sign = (Sign)b.getState();
        for(int i = 0; i < signLines.length; i++) {
            sign.setLine(i, signLines[i]);
        }
        sign.update();

        return b;
    }

    public boolean remove(BaxEntry entry)
    {
        return inventory.remove(entry);
    }

    public BaxEntry remove(int index)
    {
        return inventory.remove(index);
    }

    public void add(int index, BaxEntry entry)
    {
        entry.setInfinite(hasFlagInfinite());
        inventory.add(index, entry);
    }

    public void add(BaxEntry entry)
    {
        entry.setInfinite(hasFlagInfinite());
        inventory.add(entry);
    }
}
