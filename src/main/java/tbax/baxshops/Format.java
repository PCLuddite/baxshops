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

import org.bukkit.ChatColor;
import org.bukkit.Location;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 * 
 * This class contains the various formats for the
 * bits of information printed in chat
 * 
 */
public class Format
{   
    public static String money(String in)
    {
        return fin(ChatColor.GREEN + in);
    }
    
    public static String money(double d)
    {
        return money(Main.econ.format(d));
    }
    
    public static String money2(String in)
    {
        return fin(ChatColor.DARK_GREEN + in);
    }
    
    public static String money2(double d)
    {
        return money2(Main.econ.format(d));
    }
    
    public static String number(String n)
    {
        return fin(ChatColor.AQUA + n);
    }
    
    public static String number(int n)
    {
        return number(Integer.toString(n));
    }
    
    public static String error(String err)
    {
        return fin(ChatColor.RED + err);
    }
    
    public static String location(String loc)
    {
        return fin(ChatColor.YELLOW + loc);
    }
    
    public static String location(Location loc)
    {
        return location(String.format("(%d,%d,%d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) + ChatColor.RESET;
    }
    
    public static String itemname(int amount, String name)
    {
        return itemname(String.format("%d %s", amount, name));
    }
    
    public static String itemname(String name)
    {
        return fin(ChatColor.YELLOW + name);
    }
    
    public static String username(String name)
    {
        return fin(ChatColor.DARK_BLUE + name);
    }
    
    public static String username2(String name)
    {
        return fin(ChatColor.LIGHT_PURPLE + name);
    }
    
    public static String command(String cmd)
    {
        return fin(ChatColor.AQUA + cmd);
    }
    
    public static String retailprice(String price)
    {
        return fin(ChatColor.DARK_GREEN + price);
    }
    
    public static String retailprice(double price)
    {
        return retailprice(String.format("(%s)", Main.econ.format(price)));
    }
    
    public static String refundprice(String price)
    {
        return fin(ChatColor.BLUE + price);
    }
    
    public static String refundprice(double price)
    {
        return refundprice(String.format("(%s)", Main.econ.format(price)));
    }
    
    public static String enchantments(String ench)
    {
        return fin(ChatColor.DARK_PURPLE + ench);
    }
    
    public static String bullet(String b)
    {
        return fin(ChatColor.GRAY + b);
    }
    
    public static String bullet(int b)
    {
        return bullet(Integer.toString(b));
    }
    
    public static String listname(String name)
    {
        return fin(ChatColor.WHITE + name);
    }
    
    public static String flag(String flag)
    {
        return fin(ChatColor.YELLOW + flag);
    }
    
    public static String keyword(String word)
    {
        return fin(ChatColor.GREEN + word);
    }
    
    private static String fin(String form)
    {
        return form + ChatColor.RESET;
    }
}
