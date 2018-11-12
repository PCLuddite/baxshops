/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops.executer;

import tbax.baxshops.help.Help;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.*;
import tbax.baxshops.notification.*;
import tbax.baxshops.serialization.ItemNames;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public final class ShopExecuter
{    
    public static boolean execute(ShopCmd cmd)
    {
        switch(cmd.getAction()) {
            case "remove":
            case "rm":
                return remove(cmd);
            case "take":
            case "t":
                return take(cmd);
            case "setindex":
            case "setorder":
            case "reorder":
                return setindex(cmd);
            case "setamnt":
                return setamnt(cmd);
            case "setdur":
            case "setdurr":
                return setdur(cmd);
            case "info":
                return info(cmd);
        }
        return false;
    }
    
    public static boolean setdur(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasAdminRights();
        requisite.hasArgs(3);
        
        if (!requisite.isValid()) return true;
        
        BaxShop shop = cmd.getShop();
        BaxEntry entry;
        try {
            int index = Integer.parseInt(cmd.getArg(1));
            entry = shop.getEntryAt(index - 1);
        }
        catch (NumberFormatException e) {
            entry = ItemNames.getItemFromAlias(cmd.getArg(1), shop, cmd.getSender());
        }
        catch (IndexOutOfBoundsException e) {
            entry = null;
        }

        if (entry == null) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return true;
        }
        short amnt = -1;
        try {
            amnt = Short.parseShort(cmd.getArg(2));
        }
        catch (NumberFormatException e) {
            Main.sendError(cmd.getSender(), String.format(Resources.INVALID_DECIMAL, "damage"));
            return true;
        }
        
        entry.setDamagePercent(amnt);
        
        cmd.getMain().sendInfo(cmd.getPlayer(), "The damage has been set.");
        
        return true;
    }
    
    public static boolean setamnt(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasAdminRights();
        requisite.hasArgs(3, Help.SETAMNT);
        
        if (!requisite.isValid()) return true;
        
        BaxShop shop = cmd.getShop();
        BaxEntry entry;
        try {
            int index = Integer.parseInt(cmd.getArg(1));
            entry = shop.getEntryAt(index - 1);
        }
        catch (NumberFormatException e) {
            entry = ItemNames.getItemFromAlias(cmd.getArg(1), shop, cmd.getSender());
        }
        catch (IndexOutOfBoundsException e) {
            entry = null;
        }

        if (entry == null) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return true;
        }
        int amnt = -1;
        try {
            amnt = Integer.parseInt(cmd.getArg(2));
        }
        catch (NumberFormatException e) {
            Main.sendError(cmd.getSender(), String.format(Resources.INVALID_DECIMAL, "amount"));
            return true;
        }
        
        entry.setAmount(amnt);
        
        cmd.getMain().sendInfo(cmd.getPlayer(), "The amount has been set.");
        
        return true;
    }
    
    public static boolean remove(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasSelection();
        requisite.hasOwnership();
        requisite.hasExactArgs(2, Help.REMOVE);
        
        if (!requisite.isValid()) return true;

        BaxShop shop = cmd.getShop();
        BaxEntry entry;
        try {
            int index = Integer.parseInt(cmd.getArg(1));
            entry = shop.getEntryAt(index - 1);
        }
        catch (NumberFormatException e) {
            entry = ItemNames.getItemFromAlias(cmd.getArg(1), shop, cmd.getSender());
            if (entry == null) {
                return true;
            }
        } 
        catch (IndexOutOfBoundsException e) {
            entry = null;
        }
        if (entry == null) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return true;
        }
        
        if (!shop.infinite && entry.getAmount() > 0) {
            ItemStack stack = entry.toItemStack();
            if (!Main.tryGiveItem(cmd.getPlayer(), stack)) {
                Main.sendError(cmd.getPlayer(), Resources.NO_ROOM);
                return true;
            }

            cmd.getMain().sendInfo(cmd.getPlayer(),
                String.format("%s %s added to your inventory.",
                    Format.itemname(entry.getAmount(), ItemNames.getName(entry)),
                    entry.getAmount() == 1 ? "was" : "were"
                )
            );
        }
        shop.inventory.remove(entry);
        cmd.getMain().sendInfo(cmd.getPlayer(), "The shop entry was removed.");
        
        return true;
    }
    
    public static boolean take(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasSelection();
        requisite.hasOwnership();
        requisite.hasArgs(1, Help.TAKE);
        
        if (!requisite.isValid()) return true;
        
        BaxShop shop = cmd.getShop();
        BaxEntry entry;
        if (cmd.getNumArgs() < 2) {
            if (shop.inventory.size() == 1) {
                entry = shop.getEntryAt(0);
            }
            else {
                Main.sendError(cmd.getPlayer(), Help.TAKE.toUsageString());
                return true;
            }
        }
        else {
            try {
                int index = Integer.parseInt(cmd.getArg(1));
                entry = shop.getEntryAt(index - 1);
            }
            catch (NumberFormatException e) {
                entry = ItemNames.getItemFromAlias(cmd.getArg(1), shop, cmd.getSender());
                if (entry == null) {
                    return true;
                }
            } 
            catch (IndexOutOfBoundsException e) {
                Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
                return true;
            }
        }
        
        int amt = 1;
        if (cmd.getNumArgs() > 2) {
            try {
                amt = entry.argToAmnt(cmd.getArg(2));
            }
            catch (NumberFormatException e) {
                Main.sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "amount"));
                return true;
            }
        }
        
        if (!shop.infinite && amt > entry.getAmount()) {
            Main.sendError(cmd.getPlayer(), Resources.NO_SUPPLIES);
            return true;
        }
        
        ItemStack stack = entry.toItemStack();
        stack.setAmount(amt);
        
        entry.subtract(amt);
        
        int overflow = Main.giveItem(cmd.getPlayer(), stack);
        if (overflow > 0) {
            entry.add(overflow);
            cmd.getPlayer().sendMessage(
                String.format(Resources.SOME_ROOM,
                    amt - overflow,
                    ItemNames.getName(stack)
                )
            );
        }
        else {
            cmd.getMain().sendInfo(cmd.getPlayer(), 
                String.format("%s %s added to your inventory.",
                    Format.itemname(amt, ItemNames.getName(entry)),
                    amt == 1 ? "was" : "were"
                )
            );
        }
        return true;
    }
    
    public static boolean setindex(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasSelection();
        requisite.hasOwnership();
        requisite.hasArgs(3, Help.SETINDEX);
        
        if (!requisite.isValid()) return true;
        
        int oldIndex;
        try {
            oldIndex = Integer.parseInt(cmd.getArg(1));
        }
        catch(NumberFormatException e) {
            BaxEntry entry = ItemNames.getItemFromAlias(cmd.getArg(1), cmd.getShop(), cmd.getSender());
            if (entry == null) {
                return true;
            }
            oldIndex = cmd.getShop().getIndexOfEntry(entry) + 1;
        } 
        catch (IndexOutOfBoundsException e) {
            oldIndex = -1;
        }
        if (oldIndex < 1 || oldIndex > cmd.getShop().inventory.size()) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return true;
        }
        int newIndex;
        try {
            newIndex = Integer.parseInt(cmd.getArg(2));
        }
        catch(NumberFormatException e) {
            Main.sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "new index"));
            return true;
        }
        if (newIndex > cmd.getShop().inventory.size()) {
            Main.sendError(cmd.getPlayer(), "You must choose a new index that is less than the number of items in the shop!");
            return true;
        }
        if (newIndex < 1) {
            Main.sendError(cmd.getPlayer(), "The new index must be greater than 0.");
            return true;
        }
        if (newIndex == oldIndex) {
            Main.sendError(cmd.getPlayer(), "The index has not been changed.");
            return true;
        }
        BaxEntry entry = cmd.getShop().inventory.remove(oldIndex - 1);
        if (cmd.getShop().inventory.size() < newIndex) {
            cmd.getShop().inventory.add(entry);
        }
        else {
            cmd.getShop().inventory.add(newIndex - 1, entry);
        }
        cmd.getPlayer().sendMessage("The index for this item was successfully changed.");
        
        return true;
    }

    private static boolean info(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasSelection();
        requisite.hasPermissions("shops.buy");
        requisite.hasArgs(2, Help.INFO);
        
        if (!requisite.isValid()) return true;
        
        BaxShop shop = cmd.getShop();
        BaxEntry entry;
        try {
            int index = Integer.parseInt(cmd.getArg(1));
            entry = shop.getEntryAt(index - 1);
        }
        catch (NumberFormatException e) {
            entry = ItemNames.getItemFromAlias(cmd.getArg(1), shop, cmd.getSender());
            if (entry == null) {
                return true;
            }
        } 
        catch (IndexOutOfBoundsException e) {
            entry = null;
        }
        if (entry == null) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return true;
        }
        cmd.getPlayer().sendMessage(entry.toString());
        return true;
    }
}
