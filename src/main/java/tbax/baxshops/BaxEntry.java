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

import tbax.baxshops.help.CommandHelp;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import tbax.baxshops.serialization.ItemNames;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public final class BaxEntry implements ConfigurationSerializable
{
    private ItemStack stack;
    public double retailPrice = -1;
    public double refundPrice = -1;
    public boolean infinite = false;
    public int quantity = 0;
    
    public BaxEntry()
    {
    }
    
    public BaxEntry(Map<String, Object> args)
    {
        quantity = (int)args.get("quantity");
        retailPrice = (double)args.get("retailPrice");
        refundPrice = (double)args.get("refundPrice");
        stack = ItemStack.deserialize((Map)args.get("stack"));
    }
    
    public BaxEntry(ItemStack item)
    {
        setItem(item);
    }
        
    public Material getType()
    {
        return stack.getType();
    }
    
    @Deprecated
    public int getTypeId()
    {
        return stack.getTypeId();
    }
    
    public void add(int amt)
    {
        quantity += amt;
    }
    
    public void add(String amt)
    {
        add(convertToInteger(amt));
    }
    
    public void subtract(int amt)
    {
        quantity -= amt;
    }
    
    public void subtract(String amt)
    {
        subtract(convertToInteger(amt));
    }
    
    public void setItem(ItemStack item)
    {
        assert item != null;
        quantity = item.getAmount();
        stack = item.clone();
        stack.setAmount(1);
    }
    
    public void setItem(Material type)
    {
        stack = new ItemStack(type, 1);
    }
    
    public void setItem(Material type, short damage)
    {
        stack = new ItemStack(type, 1, damage);
    }
        
    /**
     * Converts a string amount keyword ("all","most") or number to an int
     * @param amount
     * @return 
     */
    public int convertToInteger(String amount)
    {
        if (amount.equalsIgnoreCase("all")) {
            if (infinite) {
                return 64;
            }
            else {
                return getAmount();
            }
        }
        else if (amount.equalsIgnoreCase("most")) {
            if (infinite) {
                return 64;
            }
            else {
                return getAmount() - 1;
            }
        }
        return Integer.parseInt(amount);
    }
    
    /**
     * clones this entry's item stack and sets its amount to this entry's quantity
     * If the entry quantity is equal to zero, the material type may be AIR
     * @return 
     */
    public ItemStack toItemStack()
    {
        ItemStack newstack = stack.clone();
        newstack.setAmount(quantity);
        return newstack;
    }
    
    /**
     * gets a reference to the item stack that this entry points to, which is not guaranteed to have the proper quantity
     * @return 
     */
    public ItemStack getItemStack()
    {
        return stack;
    }
    
    public Map<Enchantment, Integer> getEnchantments()
    {
        return stack.getEnchantments();
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
    
    public short getDurability()
    {
        return stack.getDurability();
    }
    
    public short getDamagePercent()
    {
        return (short)Math.round((stack.getDurability() * 100.0f) / ItemNames.getMaxDamage(stack.getType()));
    }
    
    /**
     * Tests if the item material is equal, not taking into account the quantity
     * @param item
     * @return 
     */
    public boolean isItemEqual(ItemStack item)
    {
        assert item != null;
        if (item.getAmount() > 0) {
            stack.setAmount(item.getAmount());
        }
        boolean is_equal = stack.equals(item);
        stack.setAmount(1);
        return is_equal;
    }
    
    public BaxEntry clone() // decided not to declare throwing CloneNotSupported. Java exceptions are a nightmare. 11/10/15
    {
        BaxEntry cloned = new BaxEntry();
        cloned.infinite = infinite;
        cloned.refundPrice = refundPrice;
        cloned.retailPrice = retailPrice;
        cloned.quantity = quantity;
        cloned.stack = stack.clone();
        return cloned;
    }
    
    private static Map<Enchantment, Integer> getEnchants(ItemStack item)
    {
        if (!item.getEnchantments().isEmpty()) {
            return item.getEnchantments();
        }
        else if (item.hasItemMeta()) {
            if (item.getItemMeta().hasEnchants()) {
                return item.getItemMeta().getEnchants();
            }
            else if (item.getItemMeta() instanceof EnchantmentStorageMeta) {
                return ((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants();
            }
        }
        return null;
    }
    
    private static String getPotionInfo(ItemStack item)
    {
        if (item.getType() == Material.POTION) {
            Potion p = Potion.fromItemStack(item);
            if (p.hasExtendedDuration()) {
                return Format.enchantments("(Extended)");
            }
            else if (p.getLevel() > 1) {
                return Format.enchantments("II");
            }
        }
        return "";
    }
    
    @Override
    public String toString()
    {   
        StringBuilder info = new StringBuilder();
        info.append(CommandHelp.header("BaxEntry Information"));
        info.append('\n');
        info.append("Name: ").append(Format.itemname(ItemNames.getName(this))).append('\n');
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
        if (getEnchants(stack) != null) {
            String enchants;
            Map<Enchantment, Integer> enchmap = getEnchants(stack);
            if (enchmap.isEmpty()) {
                enchants = "NONE";
            }
            else {
                StringBuilder enchsb = new StringBuilder();
                for(Map.Entry<Enchantment, Integer> enchant : enchmap.entrySet()) {
                    enchsb.append(ItemNames.getEnchantName(enchant.getKey()));
                    enchsb.append(' ');
                    enchsb.append(Format.toNumeral(enchant.getValue()));
                    enchsb.append(", ");
                }
                enchants = enchsb.substring(0, enchsb.length() - 2);
            }
            info.append("Enchants: ").append(Format.enchantments(enchants)).append('\n');
        }
        info.append("Quantity: ").append(quantity == 0 ? ChatColor.DARK_RED + "OUT OF STOCK" + ChatColor.RESET : Format.number(quantity)).append('\n');
        info.append("Buy Price: ").append(ChatColor.DARK_GREEN).append(Main.getEconomy().format(retailPrice)).append(ChatColor.RESET).append('\n');
        if (refundPrice >= 0) {
            info.append("Sell Price: ").append(ChatColor.BLUE).append(Main.getEconomy().format(refundPrice)).append(ChatColor.RESET).append('\n');
        }
        return info.toString();
    }
    
    public String toString(int index)
    {
        StringBuilder name = new StringBuilder(ItemNames.getName(this));
        
        if (getEnchants(stack) != null) {
            StringBuilder enchName = new StringBuilder(" ("); // Enchanted items are in purple
            for(Map.Entry<Enchantment, Integer> ench : getEnchants(stack).entrySet()) {
                enchName.append(ItemNames.getEnchantName(ench.getKey()).toUpperCase().substring(0,4)); // List each enchantment
                enchName.append(ench.getValue()); // and its value
                enchName.append(", "); // separated by commas
            }
            if (infinite || getAmount() > 0) {
                name.append(Format.enchantments(enchName.substring(0, enchName.length() - 2) + ")")); // Remove the last comma, put in a closing parenthesis
            }
            else {
                name.append(enchName.substring(0, enchName.length() - 2)).append(")");
            }
        }
        
        String potionInfo = getPotionInfo(stack);
        if (!potionInfo.equals("")) {
            name.append(" ").append(potionInfo);
        }
        
        if (ItemNames.isDamageable(stack.getType()) && stack.getDurability() > 0) {
            if (infinite || getAmount() > 0) {
                name.append(ChatColor.YELLOW);
            }
            name.append(" (Damage: ").append(getDamagePercent()).append("%)");
        }
        
        if (infinite) {
            if (refundPrice < 0) {
                return String.format("%s. %s %s", Format.bullet(index), Format.listname(name.toString()), Format.retailprice(retailPrice));
            }
            else {
                return String.format("%s. %s %s %s", Format.bullet(index), Format.listname(name.toString()), Format.retailprice(retailPrice), Format.refundprice(refundPrice));
            }
        }
        else {
            if (getAmount() <= 0) {
                if (refundPrice < 0) {
                    return String.format(ChatColor.RED.toString() + ChatColor.STRIKETHROUGH + "%d. (0) %s (%s)", index, name, Main.getEconomy().format(retailPrice));
                }
                else {
                    return String.format(ChatColor.RED.toString() + ChatColor.STRIKETHROUGH + "%d. (0) %s (%s) (%s)", index, name, Main.getEconomy().format(retailPrice), Main.getEconomy().format(refundPrice));
                }
            }
            else {
                if (refundPrice < 0) {
                    return String.format("%d. " + ChatColor.GRAY + "(%d) %s %s", index, getAmount(), Format.listname(name.toString()), Format.retailprice(retailPrice));
                }
                else {
                    return String.format("%d. " + ChatColor.GRAY + "(%d) %s %s %s", index, getAmount(), Format.listname(name.toString()), Format.retailprice(retailPrice), Format.refundprice(refundPrice));
                }
            }
        }
    }

    @Override
    public Map<String, Object> serialize()
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put("quantity", quantity);
        map.put("retailPrice", retailPrice);
        map.put("refundPrice", refundPrice);
        map.put("stack", stack.serialize());
        return map;
    }
    
    public static BaxEntry deserialize(Map<String, Object> args)
    {
        return new BaxEntry(args);
    }
    
    public static BaxEntry valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
