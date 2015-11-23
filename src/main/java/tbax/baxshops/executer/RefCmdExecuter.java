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
            case "paste":
                return create(cmd);
            case "list":
                return list(cmd);
            case "copy":
                return copy(cmd);
            case "tp":
            case "teleport":
                return teleport(cmd);
        }
        return false;
    }
    
    public static boolean list(ShopCmd cmd)
    {
        ShopSelection selection = cmd.getMain().selectedShops.get(cmd.getPlayer());
        if (selection == null) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
        }
        else {
            if (!cmd.getSelection().isOwner && !cmd.getPlayer().hasPermission("shops.admin")) {
                Main.sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
                return true;
            }
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
        }
        return true;
    }
    
    public static boolean teleport(ShopCmd cmd)
    {
        if (!cmd.getPlayer().hasPermission("shops.admin")) {
            Main.sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        ShopSelection selection = cmd.getMain().selectedShops.get(cmd.getPlayer());
        if (selection == null) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!cmd.ensureArgs(1)) {
            return true;
        }
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
    
    public static boolean copy(ShopCmd cmd)
    {
        ShopSelection selection = cmd.getMain().selectedShops.get(cmd.getPlayer());
        if (selection == null) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
        }
        else {
            if (!selection.isOwner && !cmd.getPlayer().hasPermission("shops.admin")) {
                Main.sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
                return true;
            }
            String id = "DEFAULT";
            if (cmd.getNumArgs() > 1) {
                id = cmd.getArg(1);
            }
            Clipboard.put(cmd.getPlayer(), id, selection.shop);
            if (id == null) {
                cmd.getPlayer().sendMessage(String.format(ChatColor.BLUE + "%s shop has been added to your clipboard.", 
                    selection.isOwner ? "Your" + ChatColor.WHITE : selection.shop.owner + ChatColor.WHITE + "'s"));
            }
            else {
                cmd.getPlayer().sendMessage(String.format("§1%s shop has been added to your clipboard as '§a%s§F'.", 
                    selection.isOwner ? "Your" + ChatColor.WHITE : selection.shop.owner + ChatColor.WHITE + "'s", id));
            }
        }
        return true;
    }
    
    public static boolean create(ShopCmd cmd)
    {
        String id = cmd.getNumArgs() > 1 ? cmd.getArg(1) : null;
        BaxShop shopSource = Clipboard.get(cmd.getPlayer(), id);
        if (shopSource == null) {
            Main.sendError(cmd.getPlayer(), String.format("No data was found on the clipboard with id '%s'.\nSelect a shop and use:\n/shop location save [id]", id == null ? "DEFAULT" : id));
            return true;
        }
        
        String owner = shopSource.owner;
        
        Block sourceLoc = shopSource.getLocations().get(0).getBlock();
        Block block;
        if (!(sourceLoc.getType().equals(Material.SIGN) || sourceLoc.getType().equals(Material.SIGN_POST))) {
            Main.log.warning(String.format(Resources.NOT_FOUND_SIGN, shopSource.owner));
            block = ShopExecuter.buildShopSign(cmd,
                new String[] {
                  "Location for",
                  (owner.length() < 13 ? owner : owner.substring(0, 12) + '…') + "'s",
                  "shop",
                  ""
            });
        }
        else {
            Sign mainSign = (Sign)sourceLoc.getState();
            block = ShopExecuter.buildShopSign(cmd,
                new String[] {
                  mainSign.getLine(0),
                  mainSign.getLine(1),
                  mainSign.getLine(2),
                  mainSign.getLine(3)
            });
        }
        if (block == null) {
            return true; // it didn't work. go back.
        }
        if (cmd.getMain().state.addLocation(cmd.getPlayer(), block.getLocation(), shopSource)) {
            shopSource.addLocation(block.getLocation());
        }
        else {
            return true; // that didn't work. go back.
        }
        cmd.getPlayer().sendMessage(String.format(ChatColor.WHITE + "A new location for " + ChatColor.BLUE + "%s" + ChatColor.WHITE + "'s shop has been opened.", shopSource.owner));
        return true;
    }
}
