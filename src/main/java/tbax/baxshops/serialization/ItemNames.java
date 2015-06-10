/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tbax.baxshops.serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.Main;

/**
 *
 * @author Timothy
 */
public class ItemNames {
    
    /**
     * A lookup table for aliases. Aliases are stored as
     * <code>alias =&gt; (ID &lt;&lt; 16) | (damageValue)</code>
     */
    public static HashMap<String, Long> aliases = new HashMap<>();
    /**
     * A lookup table for item names. Item names are stored as
     * <code>(ID &lt;&lt; 16) | (damageValue) =&gt; itemName</code>
     */
    public static HashMap<Long, String> itemNames = new HashMap<>();
    /**
     * An array of items that can be damaged
     */
    public static ArrayList<Integer> damageableIds = new ArrayList<>();
        
    /**
     * Attempts to find an item which matches the given item name (alias).
     *
     * @param alias the item name
     * @return a Long which contains the item ID and damage value as follows:
     * (id << 16) | (damage)
     */
    public static Long getItemFromAlias(String alias) {
        alias = alias.toLowerCase();
        return aliases.get(alias);
    }

    /**
     * Gets the name of an item.
     *
     * @param item an item stack
     * @return the item's name
     */
    public static String getItemName(ItemStack item) {
        return getItemName(item.getTypeId(), item.getDurability());
    }

    /**
     * Gets the name of an item.
     *
     * @param entry the shop entry
     * @return the item's name
     */
    public static String getItemName(BaxEntry entry) {
        return getItemName(entry.getType().getId(), entry.getDurability());
    }

    /**
     * Gets the name of an item.
     *
     * @param id the item's id
     * @param damage the item's damage value (durability)
     * @return the item's name
     */
    public static String getItemName(int id, int damage) {
        String name = itemNames.get((long) id << 16 | damage);
        if (name == null) {
            name = itemNames.get((long) id << 16);
            if (name == null) {
                return String.format("%d:%d", id, damage);
            }
        }
        return name;
    }
    
    
    /**
     * Loads the damageable items list from the damageable.txt resource.
     */
    public static void loadDamageable(Main main) {
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
                    damageableIds.add(Integer.parseInt(line));
                }
                catch(NumberFormatException ex) {
                }
                i++;
            }
            stream.close();
        } catch (IOException e) {
            main.log.warning("Failed to load damageable: " + e.toString());
        } catch (NoSuchElementException e) {
            main.log.info("loadAliases broke at line: " + i);
            e.printStackTrace();
        }
    }
    
    /**
     * Loads the item names map from the items.txt resource.
    */
    public static void loadItemNames(Main main) {
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
                if (line != null && line.charAt(0) == '|') {
                    do {
                        if (line.length() == 0 || line.charAt(0) == '#') {
                            continue;
                        }
                        current = new Scanner(line);
                        if (!current.next().equals("|")) {
                            break;
                        }
                        if (!current.hasNextInt(16)) {
                            break;
                        }
                        damage = current.nextInt(16);
                        name = "";
                        while (current.hasNext()) {
                            name += ' ' + current.next();
                        }
                        itemNames.put((long) id << 16 | damage, name.substring(1));
                    } while ((line = br.readLine()) != null);
                }
            }
            stream.close();
        } catch (IOException e) {
            main.log.warning("Failed to load item names: " + e.toString());
        }
    }
	
    /**
     * Loads the alias map from the aliases.txt resource.
     * @param main
     */
    public static void loadAliases(Main main) {
        InputStream stream = main.getResource("aliases.txt");
        if (stream == null) {
            return;
        }
        int i = 1;
        String name = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }
                Scanner current = new Scanner(line);
                name = current.next();
                int id = current.nextInt();
                int damage = current.hasNext() ? current.nextInt() : 0;
                aliases.put(name, (long) id << 16 | damage);
                i++;
            }
            stream.close();
        } catch (IOException e) {
            main.log.warning("Failed to load aliases: " + e.toString());
        } catch (NoSuchElementException e) {
            main.log.info("loadAliases broke at line: " + i);
            main.log.info("No such element found: " + name);
            e.printStackTrace();
        }
    }
	
}
