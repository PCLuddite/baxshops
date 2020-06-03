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
package org.tbax.baxshops.internal.items;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.internal.nms.NmsItemStack;
import org.tbax.baxshops.internal.nms.RuntimeObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.*;

public final class ItemUtil
{
    private static Map<Integer, LegacyItem> legacyItems = null;
    private static Map<Integer, Enchantment> legacyEnchants = null;

    private static final Map<Material, Material> SIGN_TO_SIGN = new HashMap<>();
    private static final List<Material> SIGN_TYPES = Arrays.asList(Material.SIGN, Material.SIGN_POST, Material.WALL_SIGN);

    static {
        SIGN_TO_SIGN.put(Material.SIGN_POST, Material.SIGN);
        SIGN_TO_SIGN.put(Material.WALL_SIGN, Material.SIGN);
    }

    /**
     * A list of enchantment names
     */
    private static final Map<Enchantment, Enchantable> enchants = new HashMap<>();
    private static final Map<PotionType, PotionInfo> potions = new HashMap<>();

    private ItemUtil()
    {
    }

    public static List<BaxEntry> getItemFromAlias(String input, BaxShop shop)
    {
        String[] words = input.toUpperCase().split("_");
        String normalizedInput = input.replace('_', ' ').toUpperCase();

        int maxMatch = -1;
        List<BaxEntry> entries = new ArrayList<>();

        for (BaxEntry entry : shop) {
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
        for (String word1 : array1) {
            for (String word2 : array2) {
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

        item = item.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(null);
            item.setItemMeta(meta);
        }
        try {
            return getNmsCopy(item).getName();
        }
        catch (ReflectiveOperationException | ClassCastException e) {
            ShopPlugin.logWarning("Could not get item name for " + item.getType());
            return item.getType().toString();
        }
    }

    private static Method asNmsCopyMethod = null;
    public static NmsItemStack getNmsCopy(ItemStack stack) throws ReflectiveOperationException
    {
        if (asNmsCopyMethod == null) {
            Class<?> craftItemStackCls = RuntimeObject.__class("org.bukkit.craftbukkit." +
                    RuntimeObject.MINECRAFT_VERSION + ".inventory.CraftItemStack");
            asNmsCopyMethod = craftItemStackCls.getMethod("asNMSCopy", ItemStack.class);
        }
        return new NmsItemStack(asNmsCopyMethod.invoke(null, stack));
    }

    public static String getEnchantName(Enchantment enchant)
    {
        Enchantable enchantable = enchants.get(enchant);
        if (enchantable == null)
            return Format.toFriendlyName(enchant.getName());
        return enchantable.getName();
    }

    /**
     * Loads the enchantment names in enchants.yml
     */
    public static void loadEnchants(ShopPlugin plugin)
    {
        try (InputStream stream = plugin.getResource("enchants.yml")) {
            YamlConfiguration enchantConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            List<Map<?, ?>> section = enchantConfig.getMapList("enchants");

            for (Map<?, ?> enchantMap : section) {
                String enchName = (String)enchantMap.get("enchantment");
                Enchantment enchantment = Enchantment.getByName(enchName);
                if (enchantment == null) {
                    ShopPlugin.logWarning(enchName + " is not an enchantment type");
                }
                else {
                    String name = (String)enchantMap.get("name");
                    Object id = enchantMap.get("id");
                    if (id instanceof Number) {
                        enchants.put(enchantment, new Enchantable(enchantment, name, ((Number)id).intValue()));
                    }
                    else {
                        enchants.put(enchantment, new Enchantable(enchantment, name));
                    }
                }
            }
        }
        catch (IOException e) {
            plugin.getLogger().warning("Failed to read enchants file: " + e.toString());
        }
    }

    /**
     * Loads the potion names in potions.yml
     */
    public static void loadPotions(ShopPlugin plugin)
    {
        try (InputStream stream = plugin.getResource("potions.yml")) {
            YamlConfiguration potionConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            List<Map<?, ?>> section = potionConfig.getMapList("potions");

            for (Map<?, ?> potionMap : section) {
                try {
                    PotionType potionType = PotionType.valueOf((String)potionMap.get("type"));
                    String name = (String)potionMap.get("name");
                    String regular = (String)potionMap.get("regular");
                    String upgraded = (String)potionMap.get("upgraded");
                    String extended = (String)potionMap.get("extended");
                    PotionInfo info = new PotionInfo(potionType, name, regular, upgraded, extended);
                    potions.put(potionType, info);
                }
                catch (IllegalArgumentException e) {
                    ShopPlugin.logWarning(potionMap.get("type") + " is not a potion type");
                }
            }
        }
        catch (IOException e) {
            plugin.getLogger().warning("Failed to read potions file: " + e.toString());
        }
    }

    public static Enchantable getEnchantable(Enchantment enchantment)
    {
        Enchantable enchantable = enchants.get(enchantment);
        if (enchantable == null)
            return new Enchantable(enchantment, Format.toFriendlyName(enchantment.getName()));
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
                    (isSameBook(stack1, stack2) || isSameBanner(stack1, stack2));
        }
        return true;
    }

    public static boolean isShop(ItemStack item)
    {
        return isSign(item) &&
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
        for (int i = 0; i < lines.length; ++i) {
            lines[i] = ChatColor.stripColor(lore.get(i));
        }
        return lines;
    }

    public static List<ItemStack> getSignTypesAsItems()
    {
        ItemStack[] stacks = new ItemStack[SIGN_TYPES.size()];
        for (int i = 0; i < SIGN_TYPES.size(); ++i) {
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
        return Material.SIGN;
    }

    public static Material toInventorySign(Material sign)
    {
        Material m = SIGN_TO_SIGN.get(sign);
        return m == null ? sign : m;
    }

    public static Map<Integer, ? extends ItemStack> all(Inventory inventory, List<ItemStack> itemStacks)
    {
        Map<Integer, ItemStack> all = new HashMap<>();
        for (int idx = 0; idx < inventory.getSize(); ++idx) {
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
        LegacyItem item = legacyItems.get(id);
        if (item == null) return null;
        return item.toItemStack(damage);
    }

    public static void loadLegacyItems(JavaPlugin plugin) throws IOException
    {
        legacyItems = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(plugin.getResource("legacy_items.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Scanner scanner = new Scanner(line);
                LegacyItem item = new LegacyItem(scanner.nextInt(), scanner.next(), scanner.nextBoolean());
                legacyItems.put(item.getItemId(), item);
            }
        }
    }

    public static void loadLegacyEnchants()
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
    }

    @Deprecated
    public static Enchantment getLegacyEnchantment(int id)
    {
        return legacyEnchants.get(id);
    }

    public static int getDurability(ItemStack stack)
    {
        return stack.getDurability();
    }

    public static void setDurability(ItemStack stack, int durability)
    {
        stack.setDurability((short)durability);
    }

    public static List<Block> getSignOnBlock(Block block)
    {
        List<Block> signs = new ArrayList<>();
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for (int z = -1; z <= 1; ++z) {
                    Location l = block.getLocation().add(x, y, z);
                    Block curr = l.getBlock();
                    if (ItemUtil.isSign(curr.getType())) {
                        if (curr.getState().getData() instanceof org.bukkit.material.Sign) {
                            org.bukkit.material.Sign sign = (org.bukkit.material.Sign)curr.getState().getData();
                            if (sign.isWallSign()) {
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
        }
        return signs;
    }

    public static boolean isWallSign(Block block)
    {
        BlockState blockState = block.getState();
        if (blockState.getData() instanceof org.bukkit.material.Sign) {
            org.bukkit.material.Sign sign = (org.bukkit.material.Sign)blockState.getData();
            return sign.isWallSign();
        }
        return false;
    }

    public static BlockFace getSignFacing(Block block)
    {
        org.bukkit.material.Sign sign = (org.bukkit.material.Sign)block.getState().getData();
        return sign.getFacing();
    }

    public static void setSignFacing(Block block, BlockFace face)
    {
        BlockState blockState = block.getState();
        org.bukkit.material.Sign sign = (org.bukkit.material.Sign)blockState.getData();
        sign.setFacingDirection(face);
        blockState.setData(sign);
        blockState.update();
    }

    public static String getNBTTag(ItemStack stack)
    {
        return new NBTTagable(stack).toString();
    }

    public static PotionInfo getNbtPotionInfo(PotionType type)
    {
        return potions.get(type);
    }

    public static String getPotionInfo(ItemStack item)
    {
        if (item.getType() == Material.POTION) {
            Potion potion = Potion.fromItemStack(item);
            if (potion.hasExtendedDuration()) {
                return Format.enchantments("(Extended)");
            }
            else if (potion.getTier() == Potion.Tier.TWO) {
                return Format.enchantments("II");
            }
        }
        return "";
    }
}