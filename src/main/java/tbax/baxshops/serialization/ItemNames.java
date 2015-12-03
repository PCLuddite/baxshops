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
package tbax.baxshops.serialization;

import java.io.*;
import java.util.*;
import tbax.baxshops.*;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

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
    private static final ArrayList<Material> damageableIds = new ArrayList<>();
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
                Main.sendError(sender, error.toString());
                return null;
            }
        }
        else {
            Main.sendError(sender, "No items could be found with that name.");
            return null;
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
        String name = itemNames.get(getItemId(item));
        if (name == null) {
            name = itemNames.get(getItemId(item.getType()));
            if (name == null) {
                name = Format.toFriendlyName(item.getData().toString());
                int last = name.lastIndexOf("Item");
                if (last > -1) {
                    name = name.substring(0, last - 1);
                }
                itemNames.put(getItemId(item), name); // save it for later
            }
        }
        return name;
    }
    
    public static String getEnchantName(Enchantment enchant)
    {
        return enchants.get(enchant);
    }
    
    /**
     * Determines if a material can be damaged
     * @param item
     * @return 
     */
    public static boolean isDamageable(Material item)
    {
        return damageableIds.contains(item);
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
                try {
                    damageableIds.add(Material.getMaterial(line));
                }
                catch(NumberFormatException ex) {
                }
                i++;
            }
            stream.close();
        }
        catch (IOException e) {
            main.log.warning("Failed to load damageable: " + e.toString());
        }
        catch (NoSuchElementException e) {
            main.log.info("loadAliases broke at line: " + i);
            e.printStackTrace();
        }
    }
    
    /**
     * Loads the item names map from the items.txt resource.
     * @param main
    */
    public static void loadItems(Main main)
    {
        InputStream stream = main.getResource("items.txt");
        if (stream == null) {
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line = br.readLine();
            while (line != null) {
                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }
                Scanner current = new Scanner(line);
                int id = current.nextInt(),
                        damage = 0;
                String name = "";
                while (current.hasNext()) {
                    name += ' ' + current.next();
                }
                if (name.length() == 0) {
                    break;
                }
                itemNames.put((long) id << 16 | damage, name.substring(1));
                line = br.readLine();
                if (line != null && (line.charAt(0) == '|' || line.charAt(0) == '+')) {
                    do {
                        if (line.length() == 0 || line.charAt(0) == '#') {
                            continue;
                        }
                        current = new Scanner(line);
                        String next = current.next();
                        if (next.equals("|")) {
                            if (!current.hasNextInt(16)) {
                                break;
                            }
                            damage = current.nextInt(16);
                        }
                        else if (next.equals("+")) {
                            if (!current.hasNextInt()) {
                                break;
                            }
                            damage = current.nextInt();
                        }
                        else {
                            break;
                        }
                        name = "";
                        while (current.hasNext()) {
                            name += ' ' + current.next();
                        }
                        itemNames.put((long) id << 16 | damage, name.substring(1));
                    } while ((line = br.readLine()) != null);
                }
            }
            stream.close();
        }
        catch (IOException e) {
            main.log.warning("Failed to load item names: " + e.toString());
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
                int id = current.nextInt();
                String name = "";
                while (current.hasNext()) {
                    name += ' ' + current.next();
                }
                if (name.length() == 0) {
                    break;
                }
                enchants.put(Enchantment.getById(id), name);
            }
            stream.close();
        }
        catch (IOException e) {
            main.log.warning("Failed to load enchants: " + e.toString());
        }
    }
}
