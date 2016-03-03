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

import tbax.baxshops.help.Help;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
            case "create":
            case "mk":
                return create(cmd);
            case "delete":
            case "del":
                return delete(cmd);
            case "add":
            case "+":
            case "ad":
                return add(cmd);
            case "restock":
            case "r":
                return restock(cmd);
            case "set":
            case "setprice":
                return set(cmd);
            case "buy":
            case "b":
                return buy(cmd);
            case "sell":
            case "s":
                return sell(cmd);
            case "remove":
            case "rm":
                return remove(cmd);
            case "take":
            case "t":
                return take(cmd);
            case "sign":
                return sign(cmd);
            case "setindex":
            case "setorder":
            case "reorder":
                return setindex(cmd);
            case "setangle":
            case "setface":
            case "face":
                return setangle(cmd);
            case "info":
                return info(cmd);
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
        
        boolean admin = requisite.isAdmin();
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
    
    public static boolean add(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasSelection();
        requisite.hasOwnership();
        requisite.hasArgs(2, Help.ADD);
        
        if (!requisite.isValid()) return true;
        
        double retailAmount, refundAmount;
        try {
            retailAmount = Math.round(100d * Double.parseDouble(cmd.getArg(1))) / 100d;
        } 
        catch (NumberFormatException e) {
            Main.sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "buy price"));
            Main.sendError(cmd.getPlayer(), Help.ADD.toUsageString());
            return true;
        }
        try {
            refundAmount = cmd.getNumArgs() > 2 ? Math.round(100d * Double.parseDouble(cmd.getArg(2))) / 100d : -1;
        }
        catch (NumberFormatException e) {
            Main.sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "sell price"));
            Main.sendError(cmd.getPlayer(), Help.ADD.toUsageString());
            return true;
        }
        
        ItemStack stack = cmd.getPlayer().getItemInHand();
        if (stack == null || stack.getType() == Material.AIR) {
            Main.sendError(cmd.getPlayer(), "You must be holding the item you wisth to add to this shop");
            return true;
        }
        if (BaxShop.isShop(stack)) {
            Main.sendError(cmd.getSender(), "You can't add a shop to a shop.");
            return true;
        }
        if (cmd.getShop().containsItem(stack)) {
            Main.sendError(cmd.getPlayer(), "That item has already been added to this shop");
            Main.sendError(cmd.getPlayer(), "Use /shop restock to restock");
            return true;
        }
        BaxEntry newEntry = new BaxEntry();
        newEntry.setItem(stack);
        newEntry.retailPrice = retailAmount;
        newEntry.refundPrice = refundAmount;
        if (cmd.getShop().infinite) {
            newEntry.infinite = true;
        }
        cmd.getShop().addEntry(newEntry);
        cmd.getMain().sendInfo(cmd.getPlayer(), 
            String.format("A new entry for %s was added to the shop.", 
                Format.itemname(newEntry.getAmount(), ItemNames.getName(newEntry))
            )
        );
        if (!cmd.getShop().infinite) {
            cmd.getPlayer().setItemInHand(null);
        }
        return true;
    }
    
    public static boolean restock(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasSelection();
        requisite.hasOwnership();
        
        if (!requisite.isValid()) return true;
        
        if (cmd.getShop().infinite) {
            Main.sendError(cmd.getPlayer(), "This shop does not need to be restocked.");
            return true;
        }
        
        if (cmd.getNumArgs() > 1 && cmd.getArg(1).equalsIgnoreCase("any")) {
            restockAny(cmd.getPlayer(), cmd.getShop());
            return true;
        }
        
        ItemStack stack = cmd.getPlayer().getItemInHand().clone();
        if (stack == null || stack.getType() == Material.AIR) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_HELDITEM);
            return true;
        }
        
        BaxEntry entry = cmd.getShop().findEntry(stack);
        if (entry == null) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return true;
        }
        
        if (cmd.getNumArgs() > 1) {
            int amt;
            try {
                amt = Integer.parseInt(cmd.getArg(1));
            }
            catch(NumberFormatException ex) {
                if (cmd.getArg(1).equalsIgnoreCase("all")) {
                    stack.setAmount(clearItems(cmd.getPlayer().getInventory(), entry));
                    entry.add(stack.getAmount());
                    cmd.getPlayer().setItemInHand(null);
                }
                else if (cmd.getArg(1).equalsIgnoreCase("most")) {
                    stack.setAmount(clearItems(cmd.getPlayer().getInventory(), entry) - 1);
                    entry.add(stack.getAmount());
                    ItemStack inHand = stack.clone();
                    inHand.setAmount(1);
                    cmd.getPlayer().setItemInHand(inHand);
                }
                else {
                    Main.sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "restock amount"));
                    Main.sendError(cmd.getPlayer(), Help.RESTOCK.toUsageString());
                    return true;
                }
                cmd.getMain().sendInfo(cmd.getPlayer(), String.format("Restocked with %s. The shop now has %s.", 
                            Format.itemname(stack.getAmount(), ItemNames.getName(entry)),
                            Format.number(entry.getAmount())
                            ));
                return true;
            }
            
            if (cmd.getPlayer().getItemInHand() != null && amt < cmd.getPlayer().getItemInHand().getAmount()) {
                stack.setAmount(amt);
                cmd.getPlayer().getItemInHand().setAmount(cmd.getPlayer().getItemInHand().getAmount() - amt); // Don't be hoggin all of it!
            }
            else {
                stack.setAmount(clearItems(cmd.getPlayer().getInventory(), entry, amt)); // Ok, take it all
            }
            
            if (stack.getAmount() < amt) {
                entry.add(stack.getAmount());
                cmd.getPlayer().setItemInHand(null);
                cmd.getMain().sendInfo(cmd.getPlayer(), String.format("Could only restock with " + ChatColor.RED + "%d %s" + ChatColor.RESET + ". The shop now has %s.",
                    stack.getAmount(), ItemNames.getName(entry),
                    Format.number(entry.getAmount()))
                );
                return true;
            }
        }
        else {
            cmd.getPlayer().setItemInHand(null);
        }
        
        entry.add(stack.getAmount());
        
        cmd.getMain().sendInfo(cmd.getPlayer(), 
            String.format("Restocked with %s in hand. The shop now has %s.", 
                Format.itemname(stack.getAmount(), ItemNames.getName(entry)),
                Format.number(entry.getAmount())
            )
        );
        return true;
    }
    
    private static void restockAny(Player player, BaxShop shop)
    {
        ArrayList<ItemStack> restocked = clearItems(player.getInventory(), shop.inventory);
        if (restocked.size() > 0) {
            for(ItemStack itemStack : restocked) {
                if (itemStack.getAmount() == 0) {
                    continue;
                }
                BaxEntry entry = shop.findEntry(itemStack);
                if (entry != null) {
                    entry.add(itemStack.getAmount());
                    player.sendMessage(String.format("Restocked %s.", 
                        Format.itemname(itemStack.getAmount(), ItemNames.getName(entry))
                    ));
                }
            }
        }
        else {
            player.sendMessage("You did not have any items that could be restocked at this shop.");
        }
    }
    
    /**
     * Removes a set number of items from an inventory
     * @param inv
     * @param entry
     * @param count
     * @return 
     */
    private static int clearItems(Inventory inv, BaxEntry entry, int count)
    {
        int i = 0;
        int addSize = 0;
        while (i < inv.getSize()) {
            if(Main.isItemEqual(inv.getItem(i), entry.getItemStack())) {
                ItemStack stack = inv.getItem(i);
                addSize += stack.getAmount();
                
                if (addSize >= count) {
                    int leftover = addSize - count;
                    if (leftover > 0) {
                        stack.setAmount(leftover);
                    }
                    return count;
                }
                else {
                    inv.clear(i);
                }
            }
            i++;
        }
        return addSize;
    }
    
    /**
     * Removes from an inventory a single item
     * @param inv
     * @param entry
     * @return The number of items that have been removed
     */
    private static int clearItems(Inventory inv, BaxEntry entry)
    {
        ArrayList<BaxEntry> entries = new ArrayList<>();
        entries.add(entry);
        ArrayList<ItemStack> list = clearItems(inv, entries);
        if (list.isEmpty()) {
            return 0;
        }
        else {
            return list.get(0).getAmount();
        }
    }
    
    /**
     * Removes from an inventory all items in the the List<BaxEntry> 
     * @param inv
     * @param entries
     * @return The items that have been removed. Each ItemStack is a different item type and may exceed the material's max stack.
     */
    private static ArrayList<ItemStack> clearItems(Inventory inv, List<BaxEntry> entries)
    {
        ArrayList<ItemStack> itemList = new ArrayList<>();
        for(BaxEntry entry : entries) {
            int i = 0;
            int addSize = 0;
            while (i < inv.getSize()){
                if(Main.isItemEqual(inv.getItem(i), entry.getItemStack())) {
                    addSize += inv.getItem(i).getAmount();
                    inv.clear(i);
                }
                i++;
            }
            if (addSize > 0) {
                ItemStack stack = entry.toItemStack();
                stack.setAmount(addSize);
                itemList.add(stack);
            }
        }
        return itemList;
    }
    
    public static boolean set(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        requisite.hasSelection();
        requisite.hasOwnership();
        requisite.hasArgs(3, Help.SET);
        
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

        double retailAmount, refundAmount;
        try {
            retailAmount = Math.round(100d * Double.parseDouble(cmd.getArg(2))) / 100d;
        } 
        catch (NumberFormatException e) {
            Main.sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL,  "buy price"));
            Main.sendError(cmd.getPlayer(), Help.SET.toUsageString());
            return true;
        }
        try {
            refundAmount = cmd.getNumArgs() > 3 ? Math.round(100d * Double.parseDouble(cmd.getArg(3))) / 100d : -1;
        } 
        catch (NumberFormatException e) {
            Main.sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL,  "sell price"));
            Main.sendError(cmd.getPlayer(), Help.SET.toUsageString());
            return true;
        }

        entry.retailPrice = retailAmount;
        entry.refundPrice = refundAmount;
        
        if (cmd.getShop().infinite) {
            cmd.getMain().sendInfo(cmd.getPlayer(),
                String.format("The price for %s was set.",
                    Format.itemname(ItemNames.getName(entry))
                )
            );
        }
        else {
            cmd.getMain().sendInfo(cmd.getPlayer(),
                String.format("The price for %s was set.",
                    Format.itemname(entry.getAmount(), ItemNames.getName(entry))
                )
            );
        }
        return true;
    }
    
    public static boolean buy(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        if (requisite.hasSelection() && cmd.getSelection().isOwner) {
            return take(cmd); // if they're the owner, use the take command
        }
        
        requisite.hasPermissions("shops.buy");
        
        if (!requisite.isValid()) {
            return true;
        }
        
        if (cmd.getNumArgs() == 1) {
            if (cmd.getShop().inventory.size() > 1) { // Allow no arguments if there's only one item  
                Main.sendError(cmd.getPlayer(), Help.BUY.toUsageString());
                return true;
            }
            else {
                cmd.appendArg("1");
            }
        }

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
        
        int amount;
        if (cmd.getNumArgs() < 3) {
            amount = 1;
        } 
        else {
            try {
                amount = entry.convertToInteger(cmd.getArg(2));
            }
            catch (NumberFormatException e) {
                Main.sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "buy amount"));
                Main.sendError(cmd.getPlayer(), Help.BUY.toUsageString());
                return true;
            }
        }
        if (amount == 0) {
            Main.sendWarning(cmd.getPlayer(), "You purchased nothing.");
            return true;
        }
        if (amount < 0) {
            Main.sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "buy amount"));
            Main.sendError(cmd.getPlayer(), Help.BUY.toUsageString());
            return true;
        }
        
        if (entry.getAmount() < amount && !shop.infinite) {
            Main.sendError(cmd.getPlayer(), Resources.NO_SUPPLIES);
            return true;
        }
        
        String itemName = ItemNames.getName(entry);
        double price = Main.roundTwoPlaces(amount * entry.retailPrice);
        
        if (!Main.getEconomy().has(cmd.getPlayer().getName(), price)) {
            Main.sendError(cmd.getPlayer(), Resources.NO_MONEY);
            return true;
        }
        
        BaxEntry purchased = entry.clone();
        purchased.setAmount(amount);
        
        if (shop.buyRequests) {
            if (!shop.infinite) {
                entry.subtract(amount);
            }
            
            BuyRequest request = new BuyRequest(shop.id, cmd.getPlayer().getName(), shop.owner, purchased);
            Main.getState().sendNotification(shop.owner, request);
            cmd.getPlayer().sendMessage(
                String.format("Your request to buy %s for %s has been sent.",
                    Format.itemname(purchased.getAmount(), itemName),
                    Format.money(price)
                )
            );
            cmd.getPlayer().sendMessage(
                String.format("This request will expire in %s days.",
                    Format.number(Resources.EXPIRE_TIME_DAYS)
                )
            );
        }
        else {
            int overflow = Main.giveItem(cmd.getPlayer(), purchased.toItemStack());
            if (overflow > 0) {
                if (overflow == amount) {
                    Main.sendError(cmd.getPlayer(), Resources.NO_ROOM);
                    return true;
                }
                price = Main.roundTwoPlaces((amount - overflow) * entry.retailPrice);
                cmd.getSender().sendMessage(
                    String.format(Resources.SOME_ROOM + " " + Resources.CHARGED_MSG,
                        amount - overflow, 
                        itemName,
                        Format.money(price)
                    )
                );
            } 
            else {
                cmd.getSender().sendMessage(
                    String.format("You bought %s for %s.",
                        Format.itemname(amount, itemName),
                        Format.money(price)
                    )
                );
            }
            Main.getEconomy().withdrawPlayer(cmd.getPlayer().getName(), price);
            if (!shop.infinite) {
                entry.subtract(amount - overflow);
            }

            Main.getEconomy().depositPlayer(shop.owner, price);

            purchased.subtract(overflow);

            cmd.getSender().sendMessage(String.format(Resources.CURRENT_BALANCE, Format.money2(Main.getEconomy().getBalance(cmd.getSender().getName()))));
            Main.getState().sendNotification(shop.owner, new BuyNotification(cmd.getPlayer().getName(), shop.owner, purchased));        
        }
        return true;
    }
    
    public static boolean sell(ShopCmd cmd)
    {
        CmdRequisite requisite = cmd.getRequirements();
        
        if (requisite.hasSelection() && cmd.getSelection().isOwner) {
            return restock(cmd); // if they're the owner, use the take command
        }
        
        requisite.hasPermissions("shops.sell");
        
        if (!requisite.isValid()) return true;
        
        if (cmd.getNumArgs() > 1 && cmd.getArg(1).equalsIgnoreCase("any")) {
            ArrayList<ItemStack> toSell = clearItems(cmd.getPlayer().getInventory(), cmd.getShop().inventory);
            if (toSell.isEmpty()) {
                Main.sendError(cmd.getPlayer(), "You did not have any items that could be sold at this shop.");
            }
            else {
                double total = 0.0;
                for(ItemStack itemStack : toSell) {
                    BaxEntry entry = cmd.getShop().findEntry(itemStack);
                    if (itemStack.getAmount() > 0 && entry != null) {
                        double price = Main.roundTwoPlaces(sell(cmd, itemStack, false));
                        if (price >= 0.0) {
                            total += price;
                        }
                    }
                }
                if (total > 0.0) {
                    cmd.getPlayer().sendMessage(String.format("You earned %s.", Format.money(total)));
                }
                cmd.getPlayer().sendMessage(String.format(Resources.CURRENT_BALANCE, Format.money2(Main.getEconomy().getBalance(cmd.getPlayer().getName()))));
            }
            return true;
        }

        ItemStack itemsToSell = cmd.getPlayer().getItemInHand().clone();
        if (itemsToSell == null || itemsToSell.getType().equals(Material.AIR)) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_HELDITEM);
            return true;
        }

        BaxShop shop = cmd.getShop();
        BaxEntry entry = shop.findEntry(itemsToSell);
        if (entry == null || entry.refundPrice < 0) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return true;
        }
        
        if (cmd.getNumArgs() > 1) {
            int actualAmt;
            try {
                int desiredAmt = Integer.parseInt(cmd.getArg(1));
                actualAmt = clearItems(cmd.getPlayer().getInventory(), entry, desiredAmt);
                if (actualAmt < desiredAmt) {
                    cmd.getPlayer().sendMessage(
                        String.format("You did not have enough to sell " + ChatColor.RED + "%d %s" + ChatColor.RESET + ", so only %s will be sold.",
                            desiredAmt,
                            desiredAmt == 1 ? "item" : "items",
                            Format.number(actualAmt)
                        )
                    );
                }
            } 
            catch (NumberFormatException e) {
                if (cmd.getArg(1).equalsIgnoreCase("all")) {
                    actualAmt = clearItems(cmd.getPlayer().getInventory(), entry);
                }
                else if (cmd.getArg(1).equalsIgnoreCase("most")) {
                    actualAmt = clearItems(cmd.getPlayer().getInventory(), entry) - 1;
                    ItemStack inHand = entry.toItemStack();
                    inHand.setAmount(1);
                    cmd.getPlayer().setItemInHand(inHand);
                    itemsToSell.setAmount(actualAmt);
                    sell(cmd, itemsToSell,true);
                    return true;
                }
                else {
                    Main.sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "amount"));
                    return true;
                }
            }
            itemsToSell.setAmount(actualAmt);
        }
        sell(cmd, itemsToSell,true);
        cmd.getPlayer().setItemInHand(null);
        return true;
    }
        
    /**
     * Sells an ItemStack
     * @param cmd shop command info
     * @param itemsToSell the item stack
     * @param showExtra true assumes this completes the transaction, false if more actions are pending
     * @return On success, the sale total is returned, otherwise -1.0 on failure
     */
    private static double sell(ShopCmd cmd, ItemStack itemsToSell, boolean showExtra)
    {
        BaxShop shop = cmd.getShop();
        BaxEntry entry = shop.findEntry(itemsToSell);
        if (entry == null || entry.refundPrice < 0) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return -1.0;
        }
        
        String name = ItemNames.getName(itemsToSell);
        
        BaxEntry req = new BaxEntry();
        req.setItem(itemsToSell);

        req.refundPrice = entry.refundPrice;
        
        SellRequest request = new SellRequest(shop.id, shop.owner, cmd.getPlayer().getName(), req);
        
        double price = Main.roundTwoPlaces((double)itemsToSell.getAmount() * entry.refundPrice);
        
        if (shop.sellRequests) {
            Main.getState().sendNotification(shop.owner, request);
            cmd.getPlayer().sendMessage(
                String.format("Your request to sell %s for %s has been sent.",
                    Format.itemname(itemsToSell.getAmount(), name),
                    Format.money(price)
                )
            );
            if (showExtra) {
                cmd.getPlayer().sendMessage(String.format("This request will expire in %s days.", Format.number(Resources.EXPIRE_TIME_DAYS)));
            }
        }
        else {
            int error = request.autoAccept(cmd.getPlayer());
            if (error == 1) {
                cmd.getPlayer().sendMessage(String.format(
                      "You have sold %s for %s to %s.",
                      Format.itemname(itemsToSell.getAmount(), name),
                      Format.money(price),
                      Format.username(shop.owner)
                    )
                );
                if (showExtra) {
                    cmd.getPlayer().sendMessage(String.format(Resources.CURRENT_BALANCE, Format.money2(Main.getEconomy().getBalance(cmd.getPlayer().getName()))));
                }
                return price;
            }
            else if (error == 0) {
                Main.getState().sendNotification(shop.owner, request);
                Main.sendError(cmd.getPlayer(), 
                    String.format("The owner could not purchase %d %s. A request has been sent to the owner to accept your offer at a later time.",
                                  itemsToSell.getAmount(), name)
                );
                if (showExtra) {
                    Main.sendError(cmd.getPlayer(), String.format("This request will expire in %d days.", Resources.EXPIRE_TIME_DAYS));
                }
            }
        }
        return -1.0;
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
                amt = entry.convertToInteger(cmd.getArg(2));
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
