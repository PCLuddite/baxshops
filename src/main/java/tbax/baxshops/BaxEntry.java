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

import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tbax.baxshops.serialization.ItemNames;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public final class BaxEntry {
    private ItemStack stack;
    public double retailPrice = -1;
    public double refundPrice = -1;
    public boolean infinite = false;
    public int quantity = 0;
    
    public BaxEntry() {
    }
    
    public BaxEntry(ItemStack item) {
        setItem(item);
    }
        
    public Material getType() {
        return stack.getType();
    }
    
    @Deprecated
    public int getTypeId() {
        return stack.getTypeId();
    }
    
    public void add(int amt) {
        quantity += amt;
    }
    
    public void add(String amt) {
        add(convertToInteger(amt));
    }
    
    public void subtract(int amt) {
        quantity -= amt;
    }
    
    public void subtract(String amt) {
        subtract(convertToInteger(amt));
    }
    
    public void setItem(ItemStack item) {
        quantity = item.getAmount();
        stack = item.clone();
        stack.setAmount(1);
    }
    
    public void setItem(Material type) {
        stack = new ItemStack(type, 1);
    }
    
    public void setItem(Material type, short damage) {
        stack = new ItemStack(type, 1, damage);
    }
        
    /**
     * Converts a string amount keyword ("all","most") or number to an int
     * @param amount
     * @return 
     */
    public int convertToInteger(String amount) {
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
    public ItemStack toItemStack() {
        ItemStack newstack = stack.clone();
        newstack.setAmount(quantity);
        return newstack;
    }
    
    /**
     * gets a reference to the item stack that this entry points to, which is not guaranteed to have the proper quantity
     * @return 
     */
    public ItemStack getItemStack() {
        return stack;
    }
    
    public Map<Enchantment, Integer> getEnchantments() {
        return stack.getEnchantments();
    }
    
    public boolean hasItemMeta() {
        return stack.hasItemMeta();
    }
    
    public ItemMeta getItemMeta() {
        return stack.getItemMeta();
    }
        
    public void setAmount(int amt) {
        quantity = amt;
    }
    
    public int getAmount() {
        return quantity;
    }
    
    public int getDurability() {
        return stack.getDurability();
    }
    
    public BaxEntry clone() { // decided not to declare throwing CloneNotSupported. Java exceptions are a nightmare. 11/10/15
        BaxEntry cloned = new BaxEntry();
        cloned.infinite = infinite;
        cloned.refundPrice = refundPrice;
        cloned.retailPrice = retailPrice;
        cloned.quantity = quantity;
        cloned.stack = stack.clone();
        return cloned;
    }
    
    public String toString(int index) {
        StringBuilder name = new StringBuilder(ItemNames.getItemName(this));
        
        if (!stack.getEnchantments().isEmpty()) {
            StringBuilder enchName = new StringBuilder(" ("); // Enchanted items are in purple
            for(Map.Entry<Enchantment, Integer> ench : stack.getEnchantments().entrySet()) {
                enchName.append(ench.getKey().getName().substring(0,3)); // List each enchantment
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
        
        if (ItemNames.isDamageable(stack.getType()) && stack.getDurability() > 0) {
            if (infinite || getAmount() > 0) {
                name.append(ChatColor.YELLOW);
            }
            name.append(" (Damage: ").append(stack.getDurability()).append(")");
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
                    return String.format(ChatColor.RED.toString() + ChatColor.STRIKETHROUGH + "%d. (0) %s (%s)", index, name, Main.econ.format(retailPrice));
                }
                else {
                    return String.format(ChatColor.RED.toString() + ChatColor.STRIKETHROUGH + "%d. (0) %s (%s) (%s)", index, name, Main.econ.format(retailPrice), Main.econ.format(refundPrice));
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
}
