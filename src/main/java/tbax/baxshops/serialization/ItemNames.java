/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops.serialization;

import java.io.*;
import java.util.*;
import tbax.baxshops.*;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.commands.PrematureAbortException;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public final class ItemNames
{
    /**
     * A lookup table for aliases. Aliases are stored as
     * <code>alias =&gt; (ID &lt;&lt; 16) | (damageValue)</code>
     */
    private static final HashMap<Long, String[]> aliases = new HashMap<>();
    /**
     * A lookup table for item names. Item names are stored as
     * <code>(ID &lt;&lt; 16) | (damageValue) =&gt; itemName</code>
     */
    private static final HashMap<Long, String> itemNames = new HashMap<>();
    /**
     * An array of items that can be damaged
     */
    private static final HashMap<Material, Short> damageable = new HashMap<>();
    /**
     * A list of enchantment names
     */
    private static final HashMap<Enchantment, String> enchants = new HashMap<>();
        
    /**
     * Attempts to find an item which matches the given item name (alias)
     *
     * @param input the item name
     * @param shop the shop
     * @param sender the sender (to show errors)
     * @return a list of entries that match the alias
     */
    public static BaxEntry getItemFromAlias(String input, BaxShop shop, CommandSender sender)
    {
        try {
            return getItemFromAlias(input, shop);
        }
        catch (PrematureAbortException e) {
            Main.sendError(sender, e.getMessage());
        }
        return  null;
    }

    public static BaxEntry getItemFromAlias(String input, BaxShop shop) throws PrematureAbortException
    {
        String[] inputwords = getItemAlias(input);
        HashMap<Double,ArrayList<BaxEntry>> match_percentages = new HashMap<>();
        double highest = 0;
        for(BaxEntry entry : shop.inventory) {
            Long id = getItemId(entry.getItemStack());
            String[] alias = aliases.get(id);
            if (alias == null) {
                alias = getItemAlias(ItemNames.getName(entry.getItemStack()));
                aliases.put(id, alias);
            }
            int matches = getNumMatches(alias, inputwords);
            double percent = (double)matches / (double)alias.length;
            ArrayList<BaxEntry> entrylist = match_percentages.get(percent);
            if (entrylist == null) {
                entrylist = new ArrayList<>();
                match_percentages.put(percent, entrylist);
            }
            entrylist.add(entry);
            if (percent > highest) {
                highest = percent;
            }
        }
        if (highest > 0) {
            ArrayList<BaxEntry> entries = match_percentages.get(highest);
            if (entries.size() == 1) {
                return entries.get(0);
            }
            else {
                StringBuilder error = new StringBuilder();
                error.append("The name '").append(input).append("' is ambiguous with the following items:\n");
                for(BaxEntry entry : entries) {
                    error.append(getName(entry)).append("\n");
                }
                error.append("BaxShops isn't sure what you want.");
                throw new PrematureAbortException(error.toString());
            }
        }
        else {
            throw new PrematureAbortException("No items could be found with that name.");
        }
    }
    
    private static String[] getItemAlias(String name)
    {
        StringBuilder alias = new StringBuilder();
        for(int index = 0; index < name.length(); ++index) {
            char c = name.charAt(index);
            if (c == ' ' || c == '_') {
                alias.append('_');
            }
            else if (Character.isAlphabetic(c)) {
                alias.append(Character.toLowerCase(c));
            }
        }
        return alias.toString().split("_");
    }
    
    private static int getNumMatches(String[] first, String[] second)
    {
        HashMap<String, Integer> map = new HashMap<>();
        for(String word : first) {
            Integer last = map.putIfAbsent(word, 1);
            if (last != null) {
                map.put(word, last + 1); // increment count
            }
        }
        int matches = 0;
        for(String word : second) {
            int count = map.getOrDefault(word, 0);
            if (count > 0) {
                --count;
                ++matches;
                map.put(word, count);
            }
        }
        return matches;
    }
    
    private static Long getItemId(ItemStack item)
    {
        return (long) item.getTypeId() << 16 | item.getDurability();
    }
    
    private static Long getItemId(Material item)
    {
        return (long)item.getId() << 16;
    }

    /**
     * Gets the name of an item.
     *
     * @param entry the shop entry
     * @return the item's name
     */
    public static String getName(BaxEntry entry)
    {
        return ItemNames.getName(entry.getItemStack());
    }

    /**
     * Gets the name of an item.
     *
     * @param item an item stack
     * @return the item's name
     */
    public static String getName(ItemStack item)
    {
        return NMSUtils.getItemName(item);
    }
    
    public static String getEnchantName(Enchantment enchant)
    {
        String name = enchants.get(enchant);
        if (name == null) {
            name = Format.toFriendlyName(enchant.getName());
        }
        return name;
    }
    
    /**
     * Determines if a material can be damaged
     * @param item
     * @return 
     */
    public static boolean isDamageable(Material item)
    {
        return damageable.get(item) != null;
    }
    
    /**
     * Gets the maximum damage for an item. This assumes damageability
     * has been confirmed with isDamageable()
     * @param item
     * @return 
     */
    public static short getMaxDamage(Material item)
    {
        return damageable.get(item);
    }
    
    /**
     * Loads the damageable items list from the damageable.txt resource.
     * @param main
     */
    public static void loadDamageable(Main main)
    {
        InputStream stream = main.getResource("damageable.txt");
        if (stream == null) {
            return;
        }
        int i = 1;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }
                Scanner scanner = new Scanner(line);
                Material material = Material.getMaterial(scanner.next());
                short maxDamage = scanner.nextShort();
                damageable.put(material, maxDamage);
                i++;
            }
            stream.close();
        }
        catch (IOException e) {
            Main.getLog().warning("Failed to load damageable: " + e.toString());
        }
        catch (NoSuchElementException e) {
            Main.getLog().info("loadDamageable broke at line: " + i);
            e.printStackTrace();
        }
    }
    
    /**
     * Loads the enchantment names in enchants.txt
     * @param main 
     */
    public static void loadEnchants(Main main)
    {
        InputStream stream = main.getResource("enchants.txt");
        if (stream == null) {
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line;
            
            while ((line = br.readLine()) != null) {
                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }
                Scanner current = new Scanner(line);
                String id = current.next();
                String name = "";
                while (current.hasNext()) {
                    name += ' ' + current.next();
                }
                if (name.length() == 0) {
                    break;
                }
                enchants.put(Enchantment.getByName(id), name.substring(1));
            }
            stream.close();
        }
        catch (IOException e) {
            Main.getLog().warning("Failed to load enchants: " + e.toString());
        }
    }
}
