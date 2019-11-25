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
package org.tbax.baxshops.items;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.ShopPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("JavaDoc")
public final class ItemUtil
{
    private static final String MINECRAFT_VERSION;
    private static final Method AS_NMS_COPY;
    private static final Method GET_NAME;

    private static Map<Integer, Material> legacyItems = null;
    private static Map<Integer, Enchantment> legacyEnchants = null;

    private static final Map<Material, Material> SIGN_TO_SIGN = new HashMap<>();

    private static final List<Material> SIGN_TYPES = Arrays.asList(Material.SPRUCE_SIGN, Material.SPRUCE_WALL_SIGN,
            Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN,
            Material.BIRCH_SIGN, Material.BIRCH_WALL_SIGN,
            Material.DARK_OAK_SIGN, Material.DARK_OAK_WALL_SIGN,
            Material.JUNGLE_SIGN, Material.JUNGLE_WALL_SIGN,
            Material.OAK_SIGN, Material.OAK_WALL_SIGN,
            Material.LEGACY_SIGN, Material.LEGACY_WALL_SIGN, Material.LEGACY_SIGN_POST);

    static {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        MINECRAFT_VERSION = name.substring(name.lastIndexOf('.') + 1);

        Method nmsCpyMthd = null;
        Method getNmMthd = null;
        try {
            Class<?> itemStackCls = Class.forName("net.minecraft.server." + MINECRAFT_VERSION + ".ItemStack");
            nmsCpyMthd = Class.forName("org.bukkit.craftbukkit." + MINECRAFT_VERSION + ".inventory.CraftItemStack")
                .getMethod("asNMSCopy", ItemStack.class);
            getNmMthd = itemStackCls.getMethod("getName");
        }
        catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        AS_NMS_COPY = nmsCpyMthd;
        GET_NAME = getNmMthd;

        SIGN_TO_SIGN.put(Material.OAK_WALL_SIGN, Material.OAK_SIGN);
        SIGN_TO_SIGN.put(Material.ACACIA_WALL_SIGN, Material.ACACIA_SIGN);
        SIGN_TO_SIGN.put(Material.BIRCH_WALL_SIGN, Material.BIRCH_SIGN);
        SIGN_TO_SIGN.put(Material.DARK_OAK_WALL_SIGN, Material.DARK_OAK_SIGN);
        SIGN_TO_SIGN.put(Material.SPRUCE_WALL_SIGN, Material.SPRUCE_SIGN);
        SIGN_TO_SIGN.put(Material.JUNGLE_WALL_SIGN, Material.JUNGLE_SIGN);
        SIGN_TO_SIGN.put(Material.LEGACY_WALL_SIGN, Material.LEGACY_SIGN);
        SIGN_TO_SIGN.put(Material.LEGACY_SIGN_POST, Material.LEGACY_SIGN);
    }

    /**
     * An array of items that can be damaged
     */
    private static final Map<Material, Short> damageable = new HashMap<>();
    /**
     * A list of enchantment names
     */
    private static final Map<Enchantment, Enchantable> enchants = new HashMap<>();

    private ItemUtil()
    {
    }

    public static List<BaxEntry> getItemFromAlias(String input, BaxShop shop)
    {
        String[] words = input.toUpperCase().split("_");
        String normalizedInput = input.replace('_', ' ').toUpperCase();

        int maxMatch = -1;
        List<BaxEntry> entries = new ArrayList<>();

        for(BaxEntry entry : shop) {
            String entryName = entry.getName().toUpperCase();
            if (Objects.equals(entryName, normalizedInput)) {
                return Collections.singletonList(entry); // 100% match
            }
            else {
                String[] entryWords = entryName.split(" ");
                int matches = getMatches(entryWords, words);
                if (matches == maxMatch) {
                    entries.add(entry);
                }
                else if (matches > maxMatch && matches > 0) {
                    entries.clear();
                    entries.add(entry);
                    maxMatch = matches;
                }
            }
        }
        return entries;
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
        return ItemUtil.getName(entry.getItemStack());
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
        else if (isOminousBanner(item)) {
            return ChatColor.GOLD + "Ominous Banner";
        }

        item = item.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(null);
            item.setItemMeta(meta);
        }
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

    public static boolean isOminousBanner(@NotNull ItemStack stack)
    {
        if (stack.getType() != Material.WHITE_BANNER)
            return false;
        BannerMeta bannerMeta = (BannerMeta)stack.getItemMeta();
        return bannerMeta.getPatterns().containsAll(ominousPatterns());
    }

    private static List<Pattern> ominousPatterns()
    {
        Pattern[] patterns = new Pattern[8];
        patterns[0] = new Pattern(DyeColor.CYAN, PatternType.RHOMBUS_MIDDLE);
        patterns[1] = new Pattern(DyeColor.LIGHT_GRAY, PatternType.STRIPE_BOTTOM);
        patterns[2] = new Pattern(DyeColor.GRAY, PatternType.STRIPE_CENTER);
        patterns[3] = new Pattern(DyeColor.LIGHT_GRAY, PatternType.BORDER);
        patterns[4] = new Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE);
        patterns[5] = new Pattern(DyeColor.LIGHT_GRAY, PatternType.HALF_HORIZONTAL);
        patterns[6] = new Pattern(DyeColor.LIGHT_GRAY, PatternType.CIRCLE_MIDDLE);
        patterns[7] = new Pattern(DyeColor.BLACK, PatternType.BORDER);
        return Arrays.asList(patterns);
    }

    public static String getEnchantName(Enchantment enchant)
    {
        Enchantable enchantable = enchants.get(enchant);
        if (enchantable == null)
            return Format.toFriendlyName(enchant.getKey().getKey());
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
                Enchantment enchantment = Enchantment.getByKey(new NamespacedKey(plugin, (String)enchantMap.get("enchantment")));
                String name = (String) enchantMap.get("name");
                boolean levels = (Boolean) enchantMap.get("levels");
                Object id = enchantMap.get("id");
                if (id instanceof Number) {
                    enchants.put(enchantment, new Enchantable(((Number)id).intValue(), name, levels));
                }
                else {
                    enchants.put(enchantment, new Enchantable(name, levels));
                }
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
            return new Enchantable(Format.toFriendlyName(enchantment.getKey().getKey()), true);
        return enchantable;
    }

    public static boolean isSameBanner(ItemStack stack1, ItemStack stack2)
    {
        BannerMeta bannerMeta1, bannerMeta2;
        if (stack1.getItemMeta() instanceof BannerMeta) {
            bannerMeta1 = (BannerMeta)stack1.getItemMeta();
        }
        else {
            return false;
        }
        if (stack2.getItemMeta() instanceof BannerMeta) {
            bannerMeta2 = (BannerMeta)stack2.getItemMeta();
        }
        else {
            return false;
        }
        if (stack1.getType() != stack2.getType())
            return false;
        if (bannerMeta1.numberOfPatterns() != bannerMeta2.numberOfPatterns())
            return false;
        for (int i = 0; i < bannerMeta1.numberOfPatterns(); ++i) {
            if (!bannerMeta1.getPattern(i).equals(bannerMeta2.getPattern(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSameBook(ItemStack stack1, ItemStack stack2)
    {
        EnchantmentStorageMeta enchantmentMeta1, enchantmentMeta2;
        if (stack1.getType() != Material.ENCHANTED_BOOK || stack2.getType() != Material.ENCHANTED_BOOK) {
            return false;
        }

        enchantmentMeta1 = (EnchantmentStorageMeta)stack1.getItemMeta();
        enchantmentMeta2 = (EnchantmentStorageMeta)stack2.getItemMeta();

        if (enchantmentMeta1.getStoredEnchants().size() != enchantmentMeta2.getStoredEnchants().size()) {
            return false;
        }

        for (Map.Entry<Enchantment, Integer> enchants : enchantmentMeta1.getStoredEnchants().entrySet()) {
            if (!Objects.equals(enchantmentMeta2.getStoredEnchants().get(enchants.getKey()), enchants.getValue())) {
                return false;
            }
        }

        return true;
    }

    public static boolean isSimilar(ItemStack stack1, ItemStack stack2, boolean smartStack)
    {
        if (stack1 == stack2) return true;
        if (stack1 == null || stack2 == null) return false;
        if (!smartStack) return stack1.isSimilar(stack2);
        if (!stack1.isSimilar(stack2)) {
            return stack1.getType() == stack2.getType() &&
                    (isSameBook(stack1, stack2)
                    || isSameBanner(stack1, stack2));
        }
        return true;
    }

    public static boolean isShop(ItemStack item)
    {
        return isSign(item)&&
               item.hasItemMeta() &&
               item.getItemMeta().hasLore() &&
               item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1).startsWith(ChatColor.GRAY + "ID: ");
    }

    public static boolean isSign(ItemStack item)
    {
        return item != null && isSign(item.getType());
    }

    public static boolean isSign(Material type)
    {
        return type != null && SIGN_TYPES.contains(type);
    }

    public static BaxShop fromItem(ItemStack item)
    {
        String id = item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1).substring((ChatColor.GRAY + "ID: ").length());
        BaxShop shop = ShopPlugin.getShopByShortId2(id); // try short id2
        if (shop == null) {
            shop = ShopPlugin.getShopByShortId(id); // try short id
            if (shop == null) {
                try {
                    return ShopPlugin.getShop(UUID.fromString(id)); // finally try full id
                }
                catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return shop;
    }

    public static String[] extractSignText(ItemStack item)
    {
        List<String> lore = item.getItemMeta().getLore().subList(0, item.getItemMeta().getLore().size() - 1);
        String[] lines = new String[lore.size()];
        for(int i = 0; i < lines.length; ++i) {
            lines[i] = ChatColor.stripColor(lore.get(i));
        }
        return lines;
    }

    public static List<Material> getSignTypes()
    {
        return SIGN_TYPES;
    }

    public static List<ItemStack> getSignTypesAsItems()
    {
        ItemStack[] stacks = new ItemStack[SIGN_TYPES.size()];
        for(int i = 0; i < SIGN_TYPES.size(); ++i) {
            stacks[i] = new ItemStack(SIGN_TYPES.get(i), 1);
        }
        return Arrays.asList(stacks);
    }

    public static ItemStack newDefaultSign()
    {
        return new ItemStack(getDefaultSignType(), 1);
    }

    public static Material getDefaultSignType()
    {
        return Material.OAK_SIGN;
    }

    public static Material toInventorySign(Material sign)
    {
        Material m = SIGN_TO_SIGN.get(sign);
        return m == null ? sign : m;
    }

    public static Map<Integer, ? extends ItemStack> all(Inventory inventory, List<ItemStack> itemStacks)
    {
        Map<Integer, ItemStack> all = new HashMap<>();
        for(int idx = 0; idx < inventory.getSize(); ++idx) {
            ItemStack item = inventory.getItem(idx);
            for (ItemStack other : itemStacks) {
                if (other.isSimilar(item)) {
                    all.put(idx, other);
                }
            }
        }
        return all;
    }

    @Deprecated
    public static ItemStack fromItemId(int id)
    {
        return fromItemId(id, (short)0);
    }

    @Deprecated
    public static ItemStack fromItemId(int id, short damage)
    {
        Material type = legacyItems.get(id);
        if (type == null) return null;
        return new ItemStack(type, 1, damage);
    }

    @Deprecated
    public static Map<Integer, Material> getLegacyItems()
    {
        return legacyItems;
    }

    @Deprecated
    public static Map<Integer, Material> loadLegacyItems(JavaPlugin plugin) throws IOException
    {
        legacyItems = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(plugin.getResource("legacy_items.txt")))) {
            String line;
            while((line = reader.readLine()) != null) {
                int idx = line.indexOf(' ');
                int id = Integer.parseInt(line.substring(0, idx));
                Material material = Material.getMaterial(line.substring(idx).trim());
                legacyItems.put(id, material);
            }
        }
        return legacyItems;
    }

    @Deprecated
    public static Map<Integer, Enchantment> loadLegacyEnchants()
    {
        legacyEnchants = new HashMap<>();
        for (Map.Entry<Enchantment, Enchantable> entry : enchants.entrySet()) {
            try {
                legacyEnchants.put(entry.getValue().getLegacyId(), entry.getKey());
            }
            catch (UnsupportedOperationException e) {
                // do not add
            }
        }
        return legacyEnchants;
    }

    @Deprecated
    public static Enchantment getLegacyEnchantment(int id) {
        return legacyEnchants.get(id);
    }

    public static int getDurability(ItemStack stack)
    {
        if (stack.getItemMeta() instanceof Damageable) {
            Damageable damage = (Damageable) stack.getItemMeta();
            return damage.getDamage();
        }
        return 0;
    }

    public static void setDurability(ItemStack stack, int durability)
    {
        if (stack.getItemMeta() instanceof Damageable) {
            Damageable damage = (Damageable) stack.getItemMeta();
            damage.setDamage(durability);
            stack.setItemMeta((ItemMeta)damage);
        }
    }

    public static List<Block> getSignOnBlock(Block block)
    {
        List<Block> signs = new ArrayList<>();
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for(int z = -1; z <= 1; ++z) {
                    Location l = block.getLocation().add(x, y, z);
                    Block curr = l.getBlock();
                    if (ItemUtil.isSign(curr.getType())) {
                        if (curr.getBlockData() instanceof WallSign) {
                            WallSign sign = (WallSign)curr.getBlockData();
                            Block attached = curr.getRelative(sign.getFacing().getOppositeFace());
                            if (attached.getLocation().equals(block.getLocation())) {
                                signs.add(curr);
                            }
                        }
                        else {
                            Location below = l.subtract(0, 1, 0);
                            if (below.equals(block.getLocation())) {
                                signs.add(curr);
                            }
                        }
                    }
                }
            }
        }
        return signs;
    }

    public static String getPotionInfo(ItemStack item)
    {
        if (item.getType() == Material.POTION) {
            PotionData data = ((PotionMeta)item.getItemMeta()).getBasePotionData();
            if (data.isExtended()) {
                return Format.enchantments("(Extended)");
            }
            else if (data.isUpgraded()) {
                return Format.enchantments("II");
            }
        }
        return "";
    }
}
