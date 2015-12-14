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
package tbax.baxshops.executer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tbax.baxshops.*;
import tbax.baxshops.serialization.Clipboard;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public final class RefCmdExecuter
{    
    public static boolean execute(ShopCmd cmd)
    {
        switch (cmd.getAction()) {
            case "list":
                return list(cmd);
            case "copy":
                return getitem(cmd);
            case "tp":
            case "teleport":
                return teleport(cmd);
        }
        return false;
    }
    
    public static boolean getitem(ShopCmd cmd)
    {
        CmdRequisite req = cmd.getRequirements();
        
        req.hasSelection();
        req.hasOwnership();
        
        if (!req.isValid())
            return true;
        
        if (!cmd.getPlayer().hasPermission("shops.admin")) {
            PlayerInventory inv = cmd.getPlayer().getInventory();
            ItemStack sign = new ItemStack(Material.SIGN, 1);
            if (!inv.containsAtLeast(sign, 1)) {
                Main.sendError(cmd.getPlayer(), "You need a sign to copy a shop.");
                return true;
            }
            inv.removeItem(sign);
        }
        
        int i = Main.giveItem(cmd.getPlayer(), cmd.getShop().toItem(Main.getSignText(cmd.getSelection().location)));
        if (i > 0) {
            cmd.getPlayer().sendMessage(Resources.NO_ROOM);
            cmd.getPlayer().getInventory().addItem(new ItemStack(Material.SIGN, 1));
        }
        
        return true;
    }
    
    public static boolean list(ShopCmd cmd)
    {
        CmdRequisite req = cmd.getRequirements();
        
        req.hasSelection();
        req.hasOwnership();
        
        if (!req.isValid())
            return true;
        
        ShopSelection selection = cmd.getMain().selectedShops.get(cmd.getPlayer());
        
        cmd.getPlayer().sendMessage(CommandHelp.header("Shop Locations"));
        if (!selection.shop.getLocations().isEmpty()) {
            cmd.getPlayer().sendMessage(
                String.format(" %-3s %-16s %-18s", ChatColor.GRAY + "#", ChatColor.WHITE + "Location", ChatColor.WHITE + "Sign Text")
            );

            for(int index = 0; index < selection.shop.getLocations().size(); ++index) {
                cmd.getPlayer().sendMessage(
                    String.format("%-3s %-16s %-18s %s",
                        ChatColor.WHITE.toString() + (index + 1) + ".", 
                        Format.location(selection.shop.getLocations().get(index)),
                        ChatColor.LIGHT_PURPLE + getSignText(selection.shop.getLocations().get(index)),
                        (selection.location.equals(selection.shop.getLocations().get(index)) ? ChatColor.LIGHT_PURPLE + " (current)" : ""))
                );
            }
        }
        else {
            cmd.getPlayer().sendMessage(ChatColor.YELLOW + "This shop has no other locations.");
        }
        return true;
    }
    
    public static boolean teleport(ShopCmd cmd)
    {
        CmdRequisite req = cmd.getRequirements();
        
        req.hasSelection();
        req.hasPermissions("shops.admin");
        req.hasArgs(2, Help.teleport);
        
        if (!req.isValid())
            return true;
        
        ShopSelection selection = cmd.getMain().selectedShops.get(cmd.getPlayer());
        
        int loc;
        try {
            loc = Integer.parseInt(cmd.getArg(1));
        }
        catch(NumberFormatException e) {
            Main.sendError(cmd.getPlayer(), "Expected a location number. For a list of locations, use /shop list.");
            return true;
        }
        if (loc < 1 || loc > selection.shop.getLocations().size()) {
            Main.sendError(cmd.getPlayer(), "That shop location does not exist.");
            return true;
        }
        
        Location old = selection.location;
        selection.location = selection.shop.getLocations().get(loc - 1);
        if (cmd.getPlayer().teleport(selection.location)) {
            cmd.getPlayer().sendMessage("Teleported you to " + Format.location(selection.location));
        }
        else {
            selection.location = old;
            Main.sendError(cmd.getPlayer(), "Unable to teleport you to that location.");
        }
        
        return true;
    }
    
    private static String getSignText(Location loc)
    {
        try {
            Sign sign = (Sign)loc.getBlock().getState();
            StringBuilder ret = new StringBuilder();
            for(int line = 0; line < 4; line++) {
                if (!sign.getLine(line).isEmpty()) {
                    if (ret.length() > 0 && ret.charAt(ret.length() - 1) != '|') {
                        ret.append("|");
                    }
                    ret.append(sign.getLine(line));
                }
            }
            if (ret.length() == 0) {
                return ChatColor.RED + "<NO TEXT>";
            }
            else {
                return ChatColor.GREEN.toString() + (ret.length() > 15 ? ret.toString().substring(0,14) : ret);
            }
        }
        catch(Exception ex){
            return ChatColor.RED + Resources.ERROR_INLINE;
        }
    }
}
