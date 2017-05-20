/* 
 * The MIT License
 *
 * Copyright © 2013-2017 Timothy Baxendale (pcluddite@hotmail.com) and 
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
package tbax.baxshops.executer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tbax.baxshops.Format;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public final class MoneyExecuter
{
    public static boolean execute(ShopCmd cmd)
    {
        switch (cmd.getAction()) {
            case "takexp":
                return takexp(cmd);
            case "givexp":
                return givexp(cmd);
        }
        return false;
    }
    
    public static boolean givexp(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasAdminRights();
        requisite.hasArgs(2);
        
        if (!requisite.isValid()) return true;
        
        CommandSender sender;
        if (cmd.getNumArgs() > 2 && cmd.isAdmin()) {
            sender = Bukkit.getPlayer(cmd.getArg(2));
        }
        else{
            sender = cmd.getSender();
        }
        
        if (!(sender instanceof Player)) {
            Main.sendError(sender, "This command can only be used by a player");
            return true;
        }
        
        int levels;
        try {
            levels = Integer.parseInt(cmd.getArg(1));
        }
        catch(NumberFormatException e) {
            Main.sendError(sender, String.format(Resources.INVALID_DECIMAL, "number of XP levels"));
            return true;
        }
        
        if (levels < 0) {
            Main.sendError(sender, String.format(Resources.INVALID_DECIMAL, "number of XP levels"));
            return true;
        }
        
        Player p;
        double money = levels * 6d;
        p = (Player)sender;
        
        Main.getEconomy().withdrawPlayer(p, money);
        p.setLevel(p.getLevel() + levels);
        
        p.sendMessage(String.format("You have been charged %s for %s levels", Format.money(money), Format.enchantments(levels + " XP")));
        
        return true;
    }
    
    public static boolean takexp(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasAdminRights();
        requisite.hasArgs(2);
        
        if (!requisite.isValid()) return true;
        
        CommandSender sender;
        if (cmd.getNumArgs() > 2 && cmd.isAdmin()) {
            sender = Bukkit.getPlayer(cmd.getArg(2));
        }
        else{
            sender = cmd.getSender();
        }
        
        if (!(sender instanceof Player)) {
            Main.sendError(sender, "This command can only be used by a player");
            return true;
        }
        
        int levels;
        try {
            levels = Integer.parseInt(cmd.getArg(1));
        }
        catch(NumberFormatException e) {
            Main.sendError(sender, String.format(Resources.INVALID_DECIMAL, "number of XP levels"));
            return true;
        }
        
        if (levels < 0) {
            Main.sendError(sender, String.format(Resources.INVALID_DECIMAL, "number of XP levels"));
            return true;
        }
        
        Player p;
        double money = levels * 6d;
        p = (Player)sender;
        
        if (levels > p.getLevel()) {
            Main.sendError(sender, "You do not have enough experience for this exchange.");
            return true;
        }
        
        Main.getEconomy().depositPlayer(p, money);
        p.setLevel(p.getLevel() - levels);
        
        p.sendMessage(String.format("You have exchanged %s levels for %s", Format.enchantments(levels + " XP"), Format.money(money)));
        
        return true;
    }
}
