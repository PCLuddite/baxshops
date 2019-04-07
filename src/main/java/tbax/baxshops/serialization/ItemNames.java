/*
 * Copyright (C) 2013-2019 Timothy Baxendale
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

package tbax.baxshops.serialization;

import javafx.fxml.FXMLLoader;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tbax.baxshops.*;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.PrematureAbortException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("JavaDoc")
public final class ItemNames
{
    private static final String MINECRAFT_VERSION;
    private static final Method AS_NMS_COPY;
    private static final Method GET_NAME;

    static {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        MINECRAFT_VERSION = name.substring(name.lastIndexOf('.') + 1);

        Method nmsCpyMthd = null;
        Method getNmMthd = null;
        try {
            Class<?> itemStackCls = Class.forName("org.bukkit.craftbukkit.ItemStack");
            nmsCpyMthd = Class.forName("org.bukkit.craftbukkit." + MINECRAFT_VERSION + ".inventory.CraftItemStack")
                .getMethod("asNMSCopy", ItemStack.class);
            getNmMthd = itemStackCls.getMethod("getName");
        }
        catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        AS_NMS_COPY = nmsCpyMthd;
        GET_NAME = getNmMthd;
    }

    /**
     * An array of items that can be damaged
     */
    private static final Map<Material, Short> damageable = new HashMap<>();
    /**
     * A list of enchantment names
     */
    private static final Map<Enchantment, Enchantable> enchants = new HashMap<>();

    private ItemNames()
    {
    }
    
    public static BaxEntry getItemFromAlias(String input, BaxShop shop) throws PrematureAbortException
    {
        String[] words = input.toUpperCase().split("_");

        int maxMatch = -1;
        List<BaxEntry> entries = new ArrayList<>();

        for(BaxEntry entry : shop) {
            String[] entryWords = entry.getName().toUpperCase().split(" ");
            int matches = getMatches(entryWords, words);
            if (matches == maxMatch) {
                entries.add(entry);
            }
            else if (matches > maxMatch) {
                entries.clear();
                entries.add(entry);
                maxMatch = matches;
            }
        }
        if (maxMatch == 0) {
            throw new CommandErrorException("No item with that name could be found");
        }
        else if (entries.size() > 1) {
            StringBuilder sb = new StringBuilder("There are multiple items that match that name:\n");
            for (BaxEntry entry : entries) {
                sb.append(entry.getName()).append('\n');
            }
            throw new CommandErrorException(sb.toString());
        }
        else {
            return entries.get(0);
        }
    }

    private static int getMatches(String[] array1, String[] array2)
    {
        int matches = 0;
        for(String word1 : array1) {
            for(String word2 : array2) {
                if (word1.equals(word2)) {
                    ++matches;
                }
            }
        }
        return matches;
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
        if (item.getType() == Material.ENCHANTED_BOOK) {
            Map<Enchantment, Integer> enchants = EnchantMap.getEnchants(item);
            if (enchants != null)
                return EnchantMap.fullListString(enchants);
        }

        item = item.clone();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(null);
        item.setItemMeta(meta);
        try {
            Object nmsCopy = AS_NMS_COPY.invoke(null, item);
            Object txtObj = GET_NAME.invoke(nmsCopy);
            try {
                return (String) txtObj;
            }
            catch (ClassCastException e) {
                return (String)txtObj.getClass().getMethod("getText").invoke(txtObj);
            }
        }
        catch (ReflectiveOperationException | ClassCastException e) {
            ShopPlugin.logWarning("Could not get item name for " + item.getType());
            return item.getType().toString();
        }
    }
    
    public static String getEnchantName(Enchantment enchant)
    {
        Enchantable enchantable = enchants.get(enchant);
        if (enchantable == null)
            return Format.toFriendlyName(enchant.getName());
        return enchantable.getName();
    }
    
    /**
     * Determines if a material can be damaged
     * @param item
     * @return 
     */
    public static boolean isDamageable(Material item)
    {
        return damageable.containsKey(item);
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
     * @param plugin
     */
    public static void loadDamageable(ShopPlugin plugin)
    {
        InputStream stream = plugin.getResource("damageable.txt");
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
            plugin.getLogger().warning("Failed to readFromDisk damageable: " + e.toString());
        }
        catch (NoSuchElementException e) {
            plugin.getLogger().info("loadDamageable broke at line: " + i);
            e.printStackTrace();
        }
    }
    
    /**
     * Loads the enchantment names in enchants.txt
     * @param plugin
     */
    public static void loadEnchants(ShopPlugin plugin)
    {
        try (InputStream stream = plugin.getResource("enchants.yml")) {
            YamlConfiguration enchantConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            List<Map<?, ?>> section = enchantConfig.getMapList("enchants");

            for (Map<?, ?> enchantMap : section) {
                Enchantment enchantment = Enchantment.getByName((String) enchantMap.get("enchantment"));
                String name = (String) enchantMap.get("name");
                boolean levels = (Boolean) enchantMap.get("levels");
                enchants.put(enchantment, new Enchantable(name, levels));
            }
        }
        catch (IOException e) {
            plugin.getLogger().warning("Failed to readFromDisk enchants: " + e.toString());
        }
    }

    public static boolean hasEnchantLevels(Enchantment enchantment)
    {
        return getEnchantable(enchantment).hasLevels();
    }

    public static Enchantable getEnchantable(Enchantment enchantment)
    {
        Enchantable enchantable = enchants.get(enchantment);
        if (enchantable == null)
            return new Enchantable(Format.toFriendlyName(enchantment.toString()), true);
        return enchantable;
    }
}
