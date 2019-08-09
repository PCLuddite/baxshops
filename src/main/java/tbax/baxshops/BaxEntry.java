/*
 * Copyright (C) 2013-2019 Timothy Baxendale
 * Portions derived from Shops Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus.
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
package tbax.baxshops;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.serialization.ItemNames;
import tbax.baxshops.serialization.UpgradeableSerialization;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.UpgradeableSerializable;

import java.util.Map;

@SuppressWarnings("unused")
public final class BaxEntry implements UpgradeableSerializable
{
    private ItemStack stack = new ItemStack(Material.AIR);
    private double retailPrice = Integer.MAX_VALUE;
    private double refundPrice = -1;
    private int quantity = 0;
    
    public BaxEntry()
    {
    }

    public BaxEntry(@NotNull BaxEntry other)
    {
        quantity = other.quantity;
        refundPrice = other.refundPrice;
        retailPrice = other.retailPrice;
        stack = other.stack.clone();
    }

    public BaxEntry(@NotNull ItemStack item)
    {
        setItem(item);
    }

    public BaxEntry(Map<String, Object> args)
    {
        UpgradeableSerialization.upgrade(this, args);
    }

    @Override
    public void upgrade00300(@NotNull SafeMap map)
    {
        retailPrice = map.getDouble("retailPrice", 10000);
        refundPrice = map.getDouble("refundPrice");
        stack = map.getItemStack("stack", new ItemStack(Material.AIR));
        quantity = map.getInteger("quantity");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void upgrade00421(@NotNull SafeMap map)
    {
        UpgradeableSerializable.super.upgrade00421(map);
        if (map.get("stack") instanceof Map) {
            stack = ItemStack.deserialize((Map) map.get("stack"));
        }
    }

    public double getRetailPrice()
    {
        return retailPrice;
    }

    public void setRetailPrice(double price)
    {
        retailPrice = price;
    }

    public double getRefundPrice()
    {
        return refundPrice;
    }

    public void setRefundPrice(double price)
    {
        refundPrice = price;
    }

    public Material getType()
    {
        return stack.getType();
    }

    public void add(int amt)
    {
        setAmount(getAmount() + amt);
    }
    
    public void subtract(int amt)
    {
        setAmount(getAmount() - amt);
    }
    
    public void setItem(@NotNull ItemStack item)
    {
        stack = item.clone();
    }

    public void setItem(@NotNull ItemStack item, int qty)
    {
        setItem(item);
        quantity = qty;
    }
    
    public void setItem(Material type)
    {
        stack = new ItemStack(type, getAmount());
    }
    
    public void setItem(Material type, int damage)
    {
        stack = new ItemStack(type, getAmount());
        setDurability(damage);
    }
        
    /**
     * clones this entry's item stack and sets its amount to this entry's quantity
     * If the entry quantity is equal to zero, the material type may be AIR
     * @return an ItemStack
     */
    public @NotNull ItemStack toItemStack()
    {
        ItemStack stack = this.stack.clone();
        stack.setAmount(quantity);
        if (quantity == 0)
            stack.setType(this.stack.getType());
        return stack;
    }
    
    /**
     * gets a reference to the ItemStack that this entry points to. the amount is not guaranteed to be the entry
     * quantity
     * @return the ItemStack
     */
    public @NotNull ItemStack getItemStack()
    {
        return stack;
    }
    
    public Map<Enchantment, Integer> getEnchantments()
    {
        return EnchantMap.getEnchants(stack);
    }
    
    public boolean hasItemMeta()
    {
        return stack.hasItemMeta();
    }
    
    public ItemMeta getItemMeta()
    {
        return stack.getItemMeta();
    }
        
    public void setAmount(int amt)
    {
        quantity = amt;
    }
    
    public int getAmount()
    {
        return quantity;
    }
    
    public int getDurability()
    {
        if (stack.getItemMeta() instanceof Damageable) {
            Damageable damage = (Damageable) stack.getItemMeta();
            return damage.getDamage();
        }
        return 0;
    }

    public void setDurability(int durability)
    {
        if (stack.getItemMeta() instanceof Damageable) {
            Damageable damage = (Damageable) stack.getItemMeta();
            damage.setDamage(durability);
            stack.setItemMeta((ItemMeta)damage);
        }
    }
    
    public int getDamagePercent()
    {
        return (int)Math.round((getDurability() * 100d) / ItemNames.getMaxDamage(stack.getType()));
    }
    
    public void setDamagePercent(int pct)
    {
        double damage = (pct / 100d) * ItemNames.getMaxDamage(stack.getType());
        if (stack.getItemMeta() instanceof Damageable) {
            setDurability((int)damage);
        }
    }

    public @NotNull String getName()
    {
        return ItemNames.getName(this);
    }

    public @NotNull String getFormattedName()
    {
        return Format.itemName(getAmount(), getName());
    }

    public @NotNull String getFormattedSellPrice()
    {
        return Format.money(MathUtil.multiply(refundPrice, getAmount()));
    }

    public @NotNull String getFormattedSellPrice2()
    {
        return Format.money2(MathUtil.multiply(refundPrice, getAmount()));
    }

    public @NotNull String getFormattedBuyPrice()
    {
        return Format.money(MathUtil.multiply(retailPrice, getAmount()));
    }

    public @NotNull String getFormattedBuyPrice2()
    {
        return Format.money2(MathUtil.multiply(retailPrice, getAmount()));
    }

    private static String getPotionInfo(ItemStack item)
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
    
    @Override
    public String toString()
    {   
        StringBuilder info = new StringBuilder();
        info.append(Format.header("BaxEntry Information"));
        info.append('\n');
        info.append("Name: ").append(Format.itemName(ItemNames.getName(this))).append('\n');
        info.append("Material: ").append(Format.command(stack.getType().toString())).append('\n');
        if (ItemNames.isDamageable(stack.getType())) {
            info.append("Damage: ").append(ChatColor.YELLOW).append(getDamagePercent()).append('%').append(ChatColor.RESET).append('\n');
        }
        if (stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            if (meta.hasDisplayName()) {
                info.append("Display Name: ").append(ChatColor.YELLOW).append(stack.getItemMeta().getDisplayName()).append(ChatColor.RESET).append('\n');
            }
            if (meta.hasLore()) {
                info.append("Description: ").append(ChatColor.BLUE);
                for (String line : meta.getLore()) {
                    info.append(line).append(' ');
                }
                info.append(ChatColor.RESET).append('\n');
            }
        }
        Map<Enchantment, Integer> enchmap = EnchantMap.getEnchants(stack);
        if (enchmap != null && !enchmap.isEmpty()) {
            info.append("Enchants: ").append(Format.enchantments(EnchantMap.fullListString(enchmap))).append('\n');
        }
        info.append("Quantity: ").append(getAmount() == 0 ? ChatColor.DARK_RED + "OUT OF STOCK" + ChatColor.RESET : Format.number(getAmount())).append('\n');
        info.append("Buy Price: ").append(ChatColor.DARK_GREEN).append(ShopPlugin.getEconomy().format(retailPrice)).append(ChatColor.RESET).append('\n');
        if (refundPrice >= 0) {
            info.append("Sell Price: ").append(ChatColor.BLUE).append(ShopPlugin.getEconomy().format(refundPrice)).append(ChatColor.RESET).append('\n');
        }
        return info.toString();
    }

    public String toString(int index, boolean infinite)
    {
        StringBuilder name;
        if(stack.getType() == Material.ENCHANTED_BOOK && EnchantMap.isEnchanted(stack)) {
            name = new StringBuilder(Format.enchantments(ItemNames.getName(this)));
        }
        else {
            name = new StringBuilder(Format.listname(ItemNames.getName(this)));
        }
        String potionInfo = getPotionInfo(stack);
        if (!potionInfo.equals("")) {
            name.append(" ").append(potionInfo);
        }
        
        if (ItemNames.isDamageable(stack.getType()) && getDurability() > 0) {
            if (infinite || getAmount() > 0) {
                name.append(ChatColor.YELLOW);
            }
            name.append(" (Damage: ").append(getDamagePercent()).append("%)");
        }

        if (stack.getType() != Material.ENCHANTED_BOOK && EnchantMap.isEnchanted(stack)) {
            name.append(" ").append(Format.enchantments("(" + EnchantMap.abbreviatedListString(stack) + ")"));
        }

        name.append(" ").append(Format.retailPrice(retailPrice));
        if (refundPrice >= 0)
            name.append(" ").append(Format.refundPrice(refundPrice));

        if (infinite) {
            return String.format("%s. %s", Format.bullet(index), Format.listname(name.toString()));
        }
        else if (getAmount() <= 0) {
            return String.format("%s. (0) %s", ChatColor.RED.toString() + ChatColor.STRIKETHROUGH + index, Format.stripColor(name.toString()));
        }
        else {
            return String.format("%d. " + ChatColor.GRAY + "(%d) %s", index, getAmount(), name.toString());
        }
    }
    
    public static BaxEntry deserialize(Map<String, Object> args)
    {
        return new BaxEntry(args);
    }
    
    public static BaxEntry valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    public boolean isSimilar(ItemStack item)
    {
        if (item == null)
            return false;
        return stack.isSimilar(item);
    }

    public boolean isSimilar(BaxEntry entry)
    {
        if (entry == null)
            return false;
        return stack.isSimilar(entry.stack);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj instanceof BaxEntry)
            return equals((BaxEntry)obj);
        if (obj instanceof ItemStack)
            return equals((ItemStack)obj);
        return false;
    }

    public boolean equals(BaxEntry entry)
    {
        if (entry == null)
            return false;
        return stack.isSimilar(entry.getItemStack()) && quantity == entry.quantity;
    }

    public boolean equals(ItemStack stack)
    {
        if (stack == null)
            return false;
        return this.stack.isSimilar(stack) && stack.getAmount() == quantity;
    }

    public String getAlias()
    {
        String name = ItemNames.getName(this).toLowerCase();
        return name.replace(' ', '_');
    }
}
