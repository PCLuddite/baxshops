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
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.serialization.ItemNames;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public class BaxEntry {
    public ItemStack stack;
    public double retailPrice = -1;
    public double refundPrice = -1;
    public boolean infinite = false;
    
    public BaxEntry() {
    }
        
    public Material getType() {
        return stack.getType();
    }
    
    public void add(int amt) {
        stack.setAmount(stack.getAmount() + amt);
    }
    
    public void add(String amt) {
        add(convertToInteger(amt));
    }
    
    public void subtract(int amt) {
        stack.setAmount(stack.getAmount() - amt);
    }
    
    public void subtract(String amt) {
        subtract(convertToInteger(amt));
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
     * clones this entry's item stack
     * @return 
     */
    public ItemStack toItemStack() {
        return stack.clone();
    }
    
    /**
     * gets a reference to the item stack that this entry points to
     * @return 
     */
    public ItemStack getItemStack() {
        return stack;
    }
    
    public Map<Enchantment, Integer> getEnchantments() {
        return stack.getEnchantments();
    }
    
    public void setItem(ItemStack item) {
        stack = item.clone();
    }
    
    public void setAmount(int amt) {
        stack.setAmount(amt);
    }
    
    public int getAmount() {
        return stack.getAmount();
    }
    
    public int getDurability() {
        return stack.getDurability();
    }
    
    public BaxEntry clone() {
        BaxEntry cloned = new BaxEntry();
        cloned.infinite = infinite;
        cloned.refundPrice = refundPrice;
        cloned.retailPrice = retailPrice;
        cloned.stack = stack.clone();
        return cloned;
    }
    
    public String toString(int index) {
        String name = ItemNames.getItemName(this);
        
        if (!stack.getEnchantments().isEmpty()) {
            StringBuilder enchName = new StringBuilder(name + " §D("); // Enchanted items are in purple
            for(Map.Entry<Enchantment, Integer> ench : stack.getEnchantments().entrySet()) {
                enchName.append(ench.getKey().getName().substring(0,3)); // List each enchantment
                enchName.append(ench.getValue()); // and its value
                enchName.append(", "); // separated by commas
            }
            name = enchName.substring(0, enchName.length() - 2) + ")"; // Remove the last comma, but in a closing parenthesis
        }
        
        if (ItemNames.damageableIds.contains(stack.getTypeId()) && stack.getDurability() > 0) {
            name += " §e(Damage: " +  stack.getDurability() + ")";
        }
        
        if (refundPrice < 0) {
            if(infinite) {
                return String.format("§7%d. §f%s §2($%.2f)", index, name, retailPrice);
            }
            else {
                return String.format(
                    stack.getAmount() == 0 ? "§C§M%d. (%d) %s §2($%.2f)" : "§f%d. §7(%d) §f%s §2($%.2f)",
                    index, stack.getAmount(), name, retailPrice);
            }
        }
        else {
            if (infinite) {
                return String.format(
                       "§7%d. §f%s §2($%.2f) §9($%.2f)",
                       index, name, retailPrice, refundPrice);
            }
            else {
                return String.format(stack.getAmount() == 0 ?
                       "§C§M%d. (%d) %s ($%.2f) ($%.2f)" :
                       "§F%d. §7(%d) §F%s §2($%.2f) §9($%.2f)",
                       index, stack.getAmount(), name, retailPrice, refundPrice);
            }
        }
    }
}
