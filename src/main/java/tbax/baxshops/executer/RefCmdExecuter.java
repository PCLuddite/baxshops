/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tbax.baxshops.executer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import tbax.baxshops.BaxShop;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.Main;
import static tbax.baxshops.Main.sendError;
import tbax.baxshops.Resources;
import tbax.baxshops.ShopSelection;

/**
 *
 * @author Timothy
 */
public class RefCmdExecuter extends CommandExecuter {
    
    public RefCmdExecuter(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }
    
    public boolean execute(String cmd, Main main) {
        if (cmd.equalsIgnoreCase("loc") || cmd.equalsIgnoreCase("location")) {
            if (args.length > 1) {
                switch(args[1].toLowerCase()) {
                    case "create":
                    case "mk":
                    case "paste":
                        return create(main);
                    case "list":
                        return list(main);
                    case "save":
                    case "copy":
                        return copy(main);
                }
            }
            else {
                return list(main);
            }
        }
        else {
            String[] original = args;
            args = Main.insertFirst(args, "location");
            switch(cmd.toLowerCase()) {
                case "paste":
                    return create(main);
                case "list":
                    return list(main);
                case "copy":
                    return copy(main);
                case "unsafe":
                    return unsafe(main);
            }
            args = original; // I don't know. Just in case that's important. It probably isn't.
        }
        return false;
    }
    
    public boolean unsafe(Main main) {
        if (args.length < 3) {
            return true;
        }
        ShopSelection selection = main.selectedShops.get(pl);
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!pl.hasPermission("shops.admin") && !selection.isOwner) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        
        switch(args[3]) {
            case "clean":
                
                break;
        }
        
        return true;
    }
    
    public boolean list(Main main) {
        ShopSelection selection = main.selectedShops.get(pl);
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
        }
        else {
            pl.sendMessage(CommandHelp.header("Shop Locations"));
            if (!selection.shop.getLocations().isEmpty()) {
                
                pl.sendMessage(
                        String.format("§7 %-3s %-16s %-18s", "#", "§fLocation", "§fSign Text")
                );
                
                for(int index = 1; index <= selection.shop.getLocations().size(); index++) {
                    pl.sendMessage(
                        String.format("§F %-3s §E%-16s §D%-18s %s",
                                index + ".", 
                                Main.formatLoc(selection.shop.getLocations().get(index - 1)),
                                getSignText(selection.shop.getLocations().get(index - 1)),
                               (selection.location.equals(selection.shop.getLocations().get(index - 1)) ? " §D(current)" : ""))
                    );
                }
            }
            else {
                pl.sendMessage("§AThis shop has no other locations.");
            }
        }
        return true;
    }
    
    private String getSignText(Location loc) {
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
                return "§C<NO TEXT>";
            }
            else {
                return ret.length() > 15 ? "§A" + ret.toString().substring(0,14) : "§A" + ret;
            }
        }
        catch(Exception ex){
            return "§C" + Resources.ERROR_INLINE;
        }
    }
    
    public boolean copy(Main main) {
        ShopSelection selection = main.selectedShops.get(pl);
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
        }
        else {
            if (!pl.hasPermission("shops.admin") && !selection.isOwner) {
                sendError(pl, Resources.NO_PERMISSION);
                return true;
            }
            String id = "DEFAULT";
            if (args.length > 2) {
                id = args[2];
            }
            clipboardPut(pl, id, selection.shop);
            if (id == null) {
                pl.sendMessage(String.format("§1%s shop has been added to your clipboard.", 
                        selection.isOwner ? "Your§f" : selection.shop.owner + "§f's"));
            }
            else {
                pl.sendMessage(String.format("§1%s shop has been added to your clipboard as '§a%s§F'.", 
                        selection.isOwner ? "Your§f" : selection.shop.owner + "§f's", id));
            }
        }
        return true;
    }
    
    public boolean create(Main main) {
        String id = args.length > 2 ? args[2] : null;
        BaxShop shopSource = clipboardGet(pl, id);
        if (shopSource == null) {
            sendError(pl, String.format("No data was found on the clipboard with id '%s'!\nSelect a shop and use:\n/shop location save [id]", id == null ? "DEFAULT" : id));
            return true;
        }
        

        String owner = shopSource.owner;
        
        Block sourceLoc = shopSource.getLocations().get(0).getBlock();
        Block block;
        if (!(sourceLoc.getType().equals(Material.SIGN) || sourceLoc.getType().equals(Material.SIGN_POST))) {
            main.log.warning(String.format(Resources.NOT_FOUND_SIGN, shopSource.owner));
            block = ShopCmdExecuter.buildShopSign(pl,
                new String[] {
                  "Location for",
                  (owner.length() < 13 ? owner : owner.substring(0, 12) + '…') + "'s",
                  "shop",
                  ""
            });
        }
        else {
            Sign mainSign = (Sign)sourceLoc.getState();
            block = ShopCmdExecuter.buildShopSign(pl,
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
        if (main.state.addLocation(pl, block.getLocation(), shopSource)) {
            shopSource.addLocation(block.getLocation());
        }
        else {
            return true; // that didn't work. go back.
        }
        pl.sendMessage(String.format("§fA new location for §1%s§f's shop has been opened.", shopSource.owner));
        return true;
    }
}
