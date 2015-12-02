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
        return reset(ChatColor.GREEN + in);
    }
    
    public static String money(double d)
    {
        return money(Main.econ.format(d));
    }
    
    public static String money2(String in)
    {
        return reset(ChatColor.DARK_GREEN + in);
    }
    
    public static String money2(double d)
    {
        return money2(Main.econ.format(d));
    }
    
    public static String number(String n)
    {
        return reset(ChatColor.AQUA + n);
    }
    
    public static String number(int n)
    {
        return number(Integer.toString(n));
    }
    
    public static String error(String err)
    {
        return reset(ChatColor.RED + err);
    }
    
    public static String location(String loc)
    {
        return reset(ChatColor.GOLD + loc);
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
        return reset(ChatColor.GREEN + name);
    }
    
    public static String username(String name)
    {
        return reset(ChatColor.DARK_BLUE + name);
    }
    
    public static String username2(String name)
    {
        return reset(ChatColor.LIGHT_PURPLE + name);
    }
    
    public static String command(String cmd)
    {
        return reset(ChatColor.AQUA + cmd);
    }
    
    public static String retailprice(String price)
    {
        return reset(ChatColor.DARK_GREEN + price);
    }
    
    public static String retailprice(double price)
    {
        return retailprice(String.format("(%s)", Main.econ.format(price)));
    }
    
    public static String refundprice(String price)
    {
        return reset(ChatColor.BLUE + price);
    }
    
    public static String refundprice(double price)
    {
        return refundprice(String.format("(%s)", Main.econ.format(price)));
    }
    
    public static String enchantments(String ench)
    {
        return reset(ChatColor.DARK_PURPLE + ench);
    }
    
    public static String bullet(String b)
    {
        return reset(ChatColor.GRAY + b);
    }
    
    public static String bullet(int b)
    {
        return bullet(Integer.toString(b));
    }
    
    public static String listname(String name)
    {
        return reset(ChatColor.WHITE + name);
    }
    
    public static String flag(String flag)
    {
        return reset(ChatColor.YELLOW + flag);
    }
    
    public static String keyword(String word)
    {
        return reset(ChatColor.GREEN + word);
    }
    
    private static String reset(String format)
    {
        return format + ChatColor.RESET;
    }
    
    /**
     * Converts a number 1-5 to a Roman numeral
     * @param n
     * @return 
     */
    public static String toNumeral(int n)
    {
        assert n > 0 && n < 6;
        switch(n) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
        }
        return null;
    }
    
    public static String toFriendlyName(String name)
    {
        if (name == null || name.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean upper = true;
        for(int index = 0; index < name.length(); ++index) {
            char c = name.charAt(index);
            switch(c) {
                case '_': // make this char a space
                    upper = true;
                    sb.append(' ');
                    break;
                case '(': // end of name
                    return sb.toString();
                case ' ':
                    upper = true;
                default:
                    if (upper) {
                        sb.append(Character.toUpperCase(c));
                        upper = false;
                    }
                    else {
                        sb.append(Character.toLowerCase(c));
                    }
                    break;
            }
        }
        return sb.toString();
    }
    
    public static String toAnsiColor(String message) // obnoxious method to convert minecraft message colors to ansi colors
    {
        StringBuilder sb = new StringBuilder();
        boolean has_ansi = false;
        for(int index = 0; index < message.length(); ++index) {
            char c = message.charAt(index);
            if (c == '§' && ++index < message.length()) {
                c = Character.toLowerCase(message.charAt(index));
                sb.append((char)27);
                sb.append("[0;");
                switch(c) {
                    case '0': sb.append("30"); break;
                    case '1': sb.append("34"); break;
                    case '2': sb.append("32"); break;
                    case '3': sb.append("36"); break;
                    case '4': sb.append("31"); break;
                    case '5': sb.append("35"); break;
                    case '6': sb.append("33"); break;
                    case '7': sb.append("37"); break;
                    case '8': sb.append("37"); break;
                    case '9': sb.append("36"); break;
                    case 'a': sb.append("32"); break;
                    case 'b': sb.append("36"); break;
                    case 'c': sb.append("31"); break;
                    case 'd': sb.append("35"); break;
                    case 'e': sb.append("33"); break;
                    case 'f': sb.append("37"); break;
                    default:
                        sb.append("37"); break;
                }
                sb.append("m");
                if (!has_ansi) {
                    has_ansi = true;
                }
            }
            else {
                sb.append(c);
            }
        }
        if (has_ansi) {
            sb.append((char)27);
            sb.append("[0m"); // reset the color
        }
        return sb.toString();
    }
}
