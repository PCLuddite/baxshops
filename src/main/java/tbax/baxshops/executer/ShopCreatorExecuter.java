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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.help.Help;

/**
 *
 * @author Timothy Baxendale
 */
public class ShopCreatorExecuter
{
    public static boolean execute(ShopCmd cmd)
    {
        switch(cmd.getAction()) {
            case "create":
            case "mk":
                return create(cmd);
            case "delete":
            case "del":
                return delete(cmd);
            case "sign":
                return sign(cmd);
            case "setangle":
            case "setface":
            case "face":
                return setangle(cmd);
        }
        return false;
    }
    
    public static boolean create(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasOwnership();
        
        if (!requisite.isValid()) {
            return true;
        }
        
        boolean admin = cmd.isAdmin();
        if (admin) {
            if (cmd.getNumArgs() < 2) {
                Main.sendError(cmd.getPlayer(), Help.CREATE.toUsageString());
                return true;
            }
        }
        
        String owner = admin ? cmd.getArg(1) : cmd.getPlayer().getName();
        
        BaxShop shop = new BaxShop();
        shop.addLocation(cmd.getPlayer().getLocation().getWorld().getBlockAt(cmd.getPlayer().getLocation()).getLocation());
        shop.owner = owner;
        
        if (buildShopSign(cmd, new String[] {
                "",
                (owner.length() < 13 ? owner : owner.substring(0, 12) + '…') + "'s",
                "shop",
                ""
            }) == null) {
            return true; // Couldn't build the sign. Retreat!
        }
        
        shop.infinite = admin && cmd.getNumArgs() > 2 && (cmd.getArg(2).equalsIgnoreCase("yes") || cmd.getArg(2).equalsIgnoreCase("true"));
        shop.sellRequests = !shop.infinite;
        shop.buyRequests = false;
        
        if (!Main.getState().addShop(cmd.getPlayer(), shop)) {
            if (!admin) {
                cmd.getPlayer().getInventory().addItem(new ItemStack(Material.SIGN)); // give the sign back
            }
            return true;
        }
        cmd.getPlayer().sendMessage(Format.username(shop.owner) + "'s shop has been created.");
        cmd.getPlayer().sendMessage(Format.flag("Buy requests") + " for this shop are " + Format.keyword(shop.buyRequests ? "on" : "off"));
        cmd.getPlayer().sendMessage(Format.flag("Sell requests") + " for this shop are " + Format.keyword(shop.sellRequests ? "on" : "off"));
        return true;
    }
    
    public static boolean delete(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasSelection();
        requisite.hasOwnership();
        
        if (!requisite.isValid()) return true;
        
        boolean deleteAll = false;
        
        if (cmd.getNumArgs() >= 3) {
            if (cmd.getArg(2).equalsIgnoreCase("all")) {
                deleteAll = true;
            }
            else {
                Main.sendError(cmd.getSender(), "Urecognized argument to /shop delete. Expected 'all' or no third argument");
                return true;
            }
        }
        
        if (cmd.getShop().getLocations().size() == 1 || deleteAll) {
            if (cmd.getShop().getInventorySize() > 0 && !cmd.getShop().infinite) {
                Main.sendError(cmd.getSender(), "There is still inventory at this shop!");
                Main.sendError(cmd.getSender(), "Please remove all inventory before deleting it.");
            }
            else {
                Main.getState().removeShop(cmd.getPlayer(), cmd.getShop());
                cmd.getMain().removeSelection(cmd.getPlayer());
            }
        }
        else {
            Main.getState().removeLocation(cmd.getPlayer(), cmd.getSelection().location);
            cmd.getMain().removeSelection(cmd.getPlayer());
        }
        return true;
    }
    
    /**
     * Builds a sign 
     * @param cmd shop command info
     * @param signLines sign next
     * @return On success, returns the block that contains the sign. Returns null on failure.
     */
    public static Block buildShopSign(ShopCmd cmd, String[] signLines)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasOwnership();
        
        if (!requisite.isValid()) return null;
        
        //Use up a sign if the user is not an admin
        if (!cmd.getPlayer().hasPermission("shops.admin")) {
            PlayerInventory inv = cmd.getPlayer().getInventory();
            ItemStack sign = new ItemStack(Material.SIGN, 1);
            if (!inv.containsAtLeast(sign, 1)) {
                Main.sendError(cmd.getPlayer(), "You need a sign to set up a shop.");
                return null;
            }
            inv.removeItem(sign);
        }
        
        Location loc = cmd.getPlayer().getLocation();
        Location locUnder = cmd.getPlayer().getLocation();
        locUnder.setY(locUnder.getY() - 1);

        Block b = loc.getWorld().getBlockAt(loc);
        Block blockUnder = locUnder.getWorld().getBlockAt(locUnder);
        if (blockUnder.getType() == Material.AIR ||
            blockUnder.getType() == Material.TNT){
                Main.sendError(cmd.getPlayer(), "You cannot place a shop on this block.");
                return null;
        }
        
        byte angle = (byte) ((((int) loc.getYaw() + 225) / 90) << 2);

        b.setType(Material.SIGN_POST);
        try {
            b.setData(angle, false);
        }
        catch(Exception e) {
        }
        if (!b.getType().equals(Material.SIGN)) {
            b.setType(Material.SIGN_POST);
            if (!b.getType().equals(Material.SIGN) && !b.getType().equals(Material.SIGN_POST)) {
                Main.sendError(cmd.getPlayer(), "Unable to place sign! Block type is " + b.getType() + ".");  
                if (!cmd.getPlayer().hasPermission("shops.admin")) {
                    cmd.getPlayer().getInventory().addItem(new ItemStack(Material.SIGN)); // give the sign back
                }
                return null;
            }
        }
        
        Sign sign = (Sign)b.getState();
        for(int i = 0; i < signLines.length; i++) {
            sign.setLine(i, signLines[i]);
        }
        sign.update();
        
        return b;
    }
    
    public static boolean setangle(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasSelection();
        requisite.hasOwnership();
        requisite.hasArgs(2, Help.SETANGLE);
        
        if (!requisite.isValid()) return true;
        
        assert cmd.getSelection().location != null;
        
        Block b = cmd.getSelection().location.getBlock();
        assert b != null;
        
        byte angle;
        try {
            angle = (byte)((Integer.parseInt(cmd.getArg(1)) % 4) << 2);
        }
        catch(NumberFormatException e) {
            switch(cmd.getArg(1).toLowerCase()) {
                case "south": angle = 0; break;
                case "west": angle = 1; break;
                case "north": angle = 2; break;
                case "east": angle = 3; break;
                default:
                    Main.sendError(cmd.getPlayer(), "The direction you entered wasn't valid! Use one of the four cardinal directions.");
                    return true;
            }
            angle = (byte)(angle << 2);
        }
        try {
            b.setData(angle, false);
            cmd.getPlayer().sendMessage("Sign rotated to face " + Format.keyword(cmd.getArg(1).toLowerCase()));
        }
        catch(Exception e) {
            Main.sendError(cmd.getPlayer(), "Some weird error occoured, and long story short, the sign may not have been rotated.");
        }
        return true;
    }
    
    public static boolean sign(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasSelection();
        requisite.hasOwnership();
        
        if (!requisite.isValid()) return true;
        
        Block b = cmd.getSelection().location.getBlock();
        if (!b.getType().equals(Material.SIGN) && !b.getType().equals(Material.SIGN_POST)) {
            Main.sendWarning(cmd.getSender(), "This shop is missing its sign.");
            cmd.getLogger().warning(String.format(Resources.NOT_FOUND_SIGN, cmd.getShop().owner));
            return true;
        }

        Sign sign = (Sign) b.getState();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < cmd.getNumArgs(); ++i) {
            sb.append(cmd.getArg(i));
            sb.append(" ");
        }
        int len = sb.length();
        if (len > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        if (len > 60) {
            Main.sendError(cmd.getPlayer(), "That text will not fit on the sign.");
            return true;
        }
        String[] lines = sb.toString().split("\\|");
        for (int i = 0; i < lines.length; ++i) {
            if (lines[i].length() > 15) {
                Main.sendError(cmd.getPlayer(), String.format("Line %d is too long. Lines can only be 15 characters in length.", i + 1));
                return true;
            }
        }
        if (lines.length < 3) {
            sign.setLine(0, "");
            sign.setLine(1, lines[0]);
            sign.setLine(2, lines.length > 1 ? lines[1] : "");
            sign.setLine(3, "");
        }
        else {
            sign.setLine(0, lines[0]);
            sign.setLine(1, lines.length > 1 ? lines[1] : "");
            sign.setLine(2, lines.length > 2 ? lines[2] : "");
            sign.setLine(3, lines.length > 3 ? lines[3] : "");
        }
        sign.update();
        return true;
    }
}
