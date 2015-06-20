/* 
 * The MIT License
 *
 * Copyright © 2015 Timothy Baxendale (pcluddite@hotmail.com) and 
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

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Help;
import tbax.baxshops.Main;
import static tbax.baxshops.Main.econ;
import static tbax.baxshops.Main.sendError;
import tbax.baxshops.Resources;
import tbax.baxshops.notification.BuyNotification;
import tbax.baxshops.notification.BuyRequest;
import tbax.baxshops.notification.SellRequest;
import tbax.baxshops.serialization.ItemNames;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public class ShopCmdExecuter {
    
    public static boolean execute(ShopCmd cmd) {
        switch(cmd.getName().toLowerCase()) {
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
        }
        return false;
    }
    
    public static boolean create(ShopCmd cmd) {
        boolean admin = cmd.getSender().hasPermission("shops.admin");
        if (admin) {
            if (cmd.getArgs().length < 2) {
                sendError(cmd.getPlayer(), Help.create.toUsageString());
                return true;
            }
        }
        
        String owner = admin ? cmd.getArgs()[1] : cmd.getPlayer().getName();
        
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
        
        shop.infinite = admin && cmd.getArgs().length > 2 && (cmd.getArgs()[2].equalsIgnoreCase("yes") || cmd.getArgs()[2].equalsIgnoreCase("true"));
        shop.sellRequests = !shop.infinite;
        shop.buyRequests = false;
        
        if (!cmd.getState().addShop(cmd.getPlayer(), shop)) {
            if (!admin) {
                cmd.getPlayer().getInventory().addItem(new ItemStack(Material.SIGN)); // give the sign back
            }
            return true;
        }
        cmd.getPlayer().sendMessage("§1" + shop.owner + "§F's shop has been created.");
        cmd.getPlayer().sendMessage("§EBuy requests §Ffor this shop are §A" + (shop.buyRequests ? "ON" : "OFF"));
        cmd.getPlayer().sendMessage("§ESell requests §Ffor this shop are §A" + (shop.sellRequests ? "ON" : "OFF"));
        return true;
    }
    
    public static Block buildShopSign(ShopCmd cmd, String[] signLines) {
        //Use up a sign if the user is not an admin
        if (!cmd.getPlayer().hasPermission("shops.admin") && !cmd.getPlayer().hasPermission("shops.owner")) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return null;
        }
        
        if (!cmd.getPlayer().hasPermission("shops.admin")) {
            PlayerInventory inv = cmd.getPlayer().getInventory();
            if (!(inv.contains(Material.SIGN))) {
                sendError(cmd.getPlayer(), "You need a sign to set up a shop.");
                return null;
            }
            inv.remove(new ItemStack(Material.SIGN, 1));
        }
        
        Location loc = cmd.getPlayer().getLocation();
        Location locUnder = cmd.getPlayer().getLocation();
        locUnder.setY(locUnder.getY() - 1);

        Block b = loc.getWorld().getBlockAt(loc);
        Block blockUnder = locUnder.getWorld().getBlockAt(locUnder);
        if (blockUnder.getType() == Material.AIR ||
            blockUnder.getType() == Material.TNT){
                sendError(cmd.getPlayer(), "You cannot cmd.getPlayer()ace a shop on this block.");
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
                sendError(cmd.getPlayer(), "Unable to cmd.getPlayer()ace sign! Block type is " + b.getType() + ".");  
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
    
    public static boolean setangle(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!cmd.getPlayer().hasPermission("shops.admin") && !cmd.getSelection().isOwner) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        if (cmd.getArgs().length < 2) {
            sendError(cmd.getPlayer(), "You must specify a direction to face!");
            return true;
        }
        if (cmd.getSelection().location == null) {
            sendError(cmd.getPlayer(), "Could not find location");
        }
        else {
            Block b = cmd.getSelection().location.getBlock();
            if (b == null) {
                sendError(cmd.getPlayer(), "Could not find block");
            }
            else {
                byte angle;
                try {
                    angle = (byte)((Integer.parseInt(cmd.getArgs()[1]) % 4) << 2);
                }
                catch(NumberFormatException e) {
                    switch(cmd.getArgs()[1].toLowerCase()) {
                        case "south": angle = 0; break;
                        case "west": angle = 1; break;
                        case "north": angle = 2; break;
                        case "east": angle = 3; break;
                        default:
                            sendError(cmd.getPlayer(), "The direction you entered wasn't valid! Use one of the four cardinal directions.");
                            return true;
                    }
                    angle = (byte)(angle << 2);
                }
                try {
                    b.setData(angle, false);
                    cmd.getPlayer().sendMessage("§fSign rotated to face §e" + cmd.getArgs()[1].toLowerCase() + "§f");
                }
                catch(Exception e) {
                    sendError(cmd.getPlayer(), "Some weird error occoured, and long story short, the sign may not have been rotated.");
                }
            }
        }
        return true;
    }
    
    private static boolean checkInventory(ShopCmd cmd) {
        if (cmd.getShop().inventory == null || cmd.getShop().inventory.isEmpty()) {
            return true;
        }
        else {
            sendError(cmd.getPlayer(), "There is still inventory at this shop!");
            sendError(cmd.getPlayer(), "Remove all inventory before deleting it.");
            return false;
        }
    }
    
    public static boolean delete(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        
        if (!cmd.getPlayer().hasPermission("shops.admin") && !cmd.getSelection().isOwner) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        
        if (cmd.getArgs().length == 3) {
            if (cmd.getArgs()[2].equalsIgnoreCase("all")) {
                if (checkInventory(cmd)) {
                    cmd.getMain().state.removeShop(cmd.getPlayer(), cmd.getShop());
                }
            }
            else {
                sendError(cmd.getPlayer(), "invalid argument '" + cmd.getArgs()[2] + "'");
            }
        }
        else {
            if (cmd.getShop().getLocations().size() == 1) {
                if (checkInventory(cmd)) {
                    cmd.getMain().state.removeShop(cmd.getPlayer(), cmd.getShop());
                    cmd.getMain().removeSelection(cmd.getPlayer());
                }
            }
            else {
                cmd.getMain().state.removeLocation(cmd.getPlayer(), cmd.getSelection().location);
            }
        }
        return true;
    }
    
    public static boolean add(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (cmd.getArgs().length < 2) {
                sendError(cmd.getPlayer(), Help.add.toUsageString());
                return true;
        }
        if (!cmd.getSelection().isOwner && !cmd.getPlayer().hasPermission("shops.admin")) {
                sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
                return true;
        }

        double retailAmount, refundAmount;
        try {
                retailAmount = Math.round(100d * Double.parseDouble(cmd.getArgs()[1])) / 100d;
        } catch (NumberFormatException e) {
                sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "buy price"));
                sendError(cmd.getPlayer(), Help.add.toUsageString());
                return true;
        }
        try {
                refundAmount = cmd.getArgs().length > 2 ? Math.round(100d * Double.parseDouble(cmd.getArgs()[2])) / 100d : -1;
        } catch (NumberFormatException e) {
                sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "sell price"));
                sendError(cmd.getPlayer(), Help.add.toUsageString());
                return true;
        }
        ItemStack stack = cmd.getPlayer().getItemInHand();
        if (stack == null || stack.getType() == Material.AIR) {
                sendError(cmd.getPlayer(), "You must be holding the item you wisth to add to this shop");
                return true;
        }
        if (cmd.getShop().containsItem(stack)) {
                sendError(cmd.getPlayer(), "That item has already been added to this shop");
                sendError(cmd.getPlayer(), "Use /shop restock to restock");
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
                String.format("§fA new entry for §a%d %s§f was added to the shop.", 
                        newEntry.getAmount(),
                        ItemNames.getItemName(newEntry)
                ));
        cmd.getPlayer().setItemInHand(null);
        return true;
    }
    
    public static boolean restock(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!cmd.getSelection().isOwner && !cmd.getPlayer().hasPermission("shops.admin") && cmd.getShop().infinite) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        if (cmd.getShop().infinite) {
            sendError(cmd.getPlayer(), "This shop does not need to be restocked.");
            return true;
        }
        
        if (cmd.getArgs().length > 1 && cmd.getArgs()[1].equalsIgnoreCase("any")) {
            restockAny(cmd.getPlayer(), cmd.getShop());
            return true;
        }
        
        ItemStack stack = cmd.getPlayer().getItemInHand().clone();
        if (stack == null || stack.getType() == Material.AIR) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_HELDITEM);
            return true;
        }
        
        BaxEntry entry = cmd.getShop().findEntry(stack);
        if (entry == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return true;
        }
        
        if (cmd.getArgs().length > 1) {
            int amt;
            try {
                amt = Integer.parseInt(cmd.getArgs()[1]);
            }
            catch(NumberFormatException ex) {
                if (cmd.getArgs()[1].equalsIgnoreCase("all")) {
                    stack.setAmount(clearItems(cmd.getPlayer().getInventory(), entry));
                    entry.add(stack.getAmount());
                    cmd.getPlayer().setItemInHand(null);
                }
                else if (cmd.getArgs()[1].equalsIgnoreCase("most")) {
                    stack.setAmount(clearItems(cmd.getPlayer().getInventory(), entry) - 1);
                    entry.add(stack.getAmount());
                    ItemStack inHand = stack.clone();
                    inHand.setAmount(1);
                    cmd.getPlayer().setItemInHand(inHand);
                }
                else {
                    sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "restock amount"));
                    sendError(cmd.getPlayer(), Help.restock.toUsageString());
                    return true;
                }
                cmd.getMain().sendInfo(cmd.getPlayer(), String.format("Restocked with §b%d %s§f. The shop now has §a%d§f.", 
                            stack.getAmount(), ItemNames.getItemName(entry),
                            entry.getAmount()
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
                cmd.getMain().sendInfo(cmd.getPlayer(), String.format("Could only restock with §c%d %s§f. You did not have enough to restock §c%d§f. The shop now has §a%d§f.",
                        stack.getAmount(), ItemNames.getItemName(entry),
                        amt,
                        entry.getAmount()));
                return true;
            }
        }
        else {
            cmd.getPlayer().setItemInHand(null);
        }
        
        entry.setAmount(entry.getAmount() + stack.getAmount());
        
        cmd.getMain().sendInfo(cmd.getPlayer(), String.format("Restocked with §b%d %s§f in hand. The shop now has §a%d§f.", 
                        stack.getAmount(), ItemNames.getItemName(entry),
                        entry.getAmount()
                        ));
        return true;
    }
    
    private static void restockAny(Player player, BaxShop shop) {
        ArrayList<ItemStack> restocked = clearItems(player.getInventory(), shop.inventory);
        if (restocked.size() > 0) {
            for(ItemStack itemStack : restocked) {
                if (itemStack.getAmount() == 0) {
                    continue;
                }
                BaxEntry entry = shop.findEntry(itemStack);
                if (entry != null) {
                    entry.setAmount(entry.getAmount() + itemStack.getAmount());
                    player.sendMessage(String.format("§fRestocked §b%d %s§f.", 
                            itemStack.getAmount(), ItemNames.getItemName(entry)
                            ));
                }
            }
        }
        else {
            sendError(player, "You did not have any items that could be restocked at this shop.");
        }
    }
    
    private static int getAmount(String input, BaxEntry entry) {
        return getAmount(input, entry.getItemStack(), entry.infinite);
    }
    
    private static int getAmount(String input, ItemStack entry, boolean infinite) {
        if (input.equalsIgnoreCase("all")) {
            if (infinite) {
                return 64;
            }
            else {
                return entry.getAmount();
            }
        }
        else if (input.equalsIgnoreCase("most")) {
            if (infinite) {
                return 64;
            }
            else {
                return entry.getAmount() - 1;
            }
        }
        return Integer.parseInt(input);
    }
    
    private static int clearItems(Inventory inv, BaxEntry entry, int count) {
        int i = 0;
        int addSize = 0;
        while (i < inv.getSize()){
            if(inv.getItem(i) != null &&
                inv.getItem(i).getType() != Material.AIR &&
                inv.getItem(i).getType() == entry.getType() &&
                inv.getItem(i).getDurability() == entry.getDurability()) {

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
    
    private static int clearItems(Inventory inv, BaxEntry entry) {
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
    
    private static ArrayList<ItemStack> clearItems(Inventory inv, ArrayList<BaxEntry> entries) {
        ArrayList<ItemStack> itemList = new ArrayList<>();
        for(BaxEntry entry : entries) {
            int i = 0;
            int addSize = 0;
            while (i < inv.getSize()){
                if(inv.getItem(i) != null &&
                    inv.getItem(i).getType() != Material.AIR &&
                    inv.getItem(i).getType() == entry.getType() &&
                    inv.getItem(i).getDurability() == entry.getDurability()) {
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
    
    public static boolean set(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!cmd.getSelection().isOwner && !cmd.getPlayer().hasPermission("shops.admin")) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        
        if (cmd.getArgs().length < 3) {
            sendError(cmd.getPlayer(), Help.set.toUsageString());
            return true;
        }
        BaxShop shop = cmd.getShop();
        BaxEntry entry;
        try {
            int index = Integer.parseInt(cmd.getArgs()[1]);
            entry = shop.getEntryAt(index - 1);
        }
        catch (NumberFormatException e) {
            Long item = ItemNames.getItemFromAlias(cmd.getArgs()[1]);
            if (item == null) {
                sendError(cmd.getPlayer(), Resources.NOT_FOUND_ALIAS);
                return true;
            }
            int id = (int) (item >> 16);
            int damage = (short) (item & 0xFFFF);
            entry = shop.findEntry(Material.getMaterial(id), damage);
        }
        catch (IndexOutOfBoundsException e) {
            entry = null;
        }

        if (entry == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return true;
        }

        double retailAmount, refundAmount;
        try {
            retailAmount = Math.round(100d * Double.parseDouble(cmd.getArgs()[2])) / 100d;
        } 
        catch (NumberFormatException e) {
            sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL,  "buy price"));
            sendError(cmd.getPlayer(), Help.set.toUsageString());
            return true;
        }
        try {
            refundAmount = cmd.getArgs().length > 3 ? Math.round(100d * Double.parseDouble(cmd.getArgs()[3])) / 100d : -1;
        } 
        catch (NumberFormatException e) {
            sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL,  "sell price"));
            sendError(cmd.getPlayer(), Help.set.toUsageString());
            return true;
        }

        entry.retailPrice = retailAmount;
        entry.refundPrice = refundAmount;
        
        cmd.getMain().sendInfo(cmd.getPlayer(), String.format("§fThe price for §a%d %s§F was set.",
                entry.getAmount(), ItemNames.getItemName(entry)));
        return true;
    }
    
    public static boolean buy(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!cmd.getPlayer().hasPermission("shops.buy")) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        if (cmd.getArgs().length < 1) {
            sendError(cmd.getPlayer(), Help.buy.toUsageString());
            return true;
        }
        if (cmd.getArgs().length == 1) {
            if (cmd.getShop().inventory.size() > 1) { // Allow no arguments if there's only one item  
                sendError(cmd.getPlayer(), Help.buy.toUsageString());
                return true;
            }
            else {
                cmd.setArgs(new String[] {
                  cmd.getArgs()[0], "1" 
                });
            }
        }

        BaxShop shop = cmd.getShop();
        BaxEntry entry;
        try {
            int index = Integer.parseInt(cmd.getArgs()[1]);
            entry = shop.getEntryAt(index - 1);
        } 
        catch (NumberFormatException e) {
            Long item = ItemNames.getItemFromAlias(cmd.getArgs()[1]);
            if (item == null) {
                sendError(cmd.getPlayer(), Resources.NOT_FOUND_ALIAS);
                return true;
            }
            int id = (int) (item >> 16);
            short damage = (short) (item & 0xFFFF);
            entry = shop.findEntry(Material.getMaterial(id), damage);
        } 
        catch (IndexOutOfBoundsException e) {
            entry = null;
        }
        if (entry == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return true;
        }
        
        int amount;
        if (cmd.getArgs().length < 3) {
            amount = 1;
        } 
        else {
            try {
                amount = getAmount(cmd.getArgs()[2], entry.getItemStack(), entry.infinite);
            }
            catch (NumberFormatException e) {
                sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "buy amount"));
                sendError(cmd.getPlayer(), Help.buy.toUsageString());
                return true;
            }
        }
        if (amount == 0) {
            sendError(cmd.getPlayer(), "Congrats. You bought nothing.");
            return true;
        }
        if (amount < 0) {
            sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "buy amount"));
            sendError(cmd.getPlayer(), Help.buy.toUsageString());
            return true;
        }
        
        if (entry.getAmount() < amount && !shop.infinite) {
            sendError(cmd.getPlayer(), Resources.NO_SUPPLIES);
            return true;
        }
        
        String itemName = ItemNames.getItemName(entry);
        double price = Main.roundTwoPlaces(amount * entry.retailPrice);
        
        if (!econ.has(cmd.getPlayer().getName(), price)) {
            sendError(cmd.getPlayer(), Resources.NO_MONEY);
            return true;
        }
        
        BaxEntry purchased = new BaxEntry();
        purchased.setItem(entry.toItemStack());
        purchased.retailPrice = entry.retailPrice;
        purchased.refundPrice = entry.refundPrice;
        purchased.setAmount(amount);
        
        if (shop.buyRequests) {
            if (!shop.infinite) {
                entry.setAmount(entry.getAmount() - amount);
            }
            
            BuyRequest request = new BuyRequest(shop, purchased, cmd.getPlayer().getName());
            cmd.getMain().state.sendNotification(shop.owner, request);
            cmd.getPlayer().sendMessage(String.format("§FYour request to buy §e%d %s§F for §a$%.2f§F has been sent.",
                           purchased.getAmount(), itemName, price));
            cmd.getPlayer().sendMessage(String.format("§FThis request will expire in %d days.", Resources.EXPIRE_TIME_DAYS));
            return true;
        }
        
        HashMap<Integer, ItemStack> overflow = cmd.getPlayer().getInventory().addItem(purchased.toItemStack());
        int refunded = 0;
        if (overflow.size() > 0) {
            refunded = overflow.get(0).getAmount();
            if (overflow.size() == amount) {
                sendError(cmd.getPlayer(), Resources.NO_ROOM);
                return true;
            }
            price = Main.roundTwoPlaces((amount - refunded) * entry.retailPrice);
            cmd.getSender().sendMessage(String.format(Resources.SOME_ROOM,
                amount - refunded, itemName, price));
        } 
        else {
            cmd.getSender().sendMessage(String.format("You bought §e%d %s§F for §a$%.2f§F.",
                amount, itemName, price));
        }
        econ.withdrawPlayer(cmd.getPlayer().getName(), price);
        if (!shop.infinite) {
            entry.setAmount(entry.getAmount() - (amount - refunded));
        }

        econ.depositPlayer(shop.owner, price);
        
        purchased.setAmount(amount - refunded);
        
        cmd.getSender().sendMessage(String.format(Resources.CURRENT_BALANCE, Main.econ.format(Main.econ.getBalance(cmd.getSender().getName()))));
        cmd.getMain().state.sendNotification(shop.owner, new BuyNotification(shop, purchased, cmd.getPlayer().getName()));        
        
        return true;
    }
    
    public static boolean sell(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!cmd.getPlayer().hasPermission("shops.sell")) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        if (cmd.getSelection().isOwner && !cmd.getPlayer().hasPermission("shops.self")) {
            sendError(cmd.getPlayer(), "You cannot sell items to yourself.");
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        
        if (cmd.getArgs().length > 1 && cmd.getArgs()[1].equalsIgnoreCase("any")) {
            ArrayList<ItemStack> toSell = clearItems(cmd.getPlayer().getInventory(), cmd.getShop().inventory);
            if (toSell.isEmpty()) {
                sendError(cmd.getPlayer(), "You did not have any items that could be sold at this shop.");
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
                    cmd.getPlayer().sendMessage(String.format("§fYou earned §a$%.2f§f", total));
                }
                cmd.getPlayer().sendMessage(String.format(Resources.CURRENT_BALANCE, Main.econ.format(Main.econ.getBalance(cmd.getPlayer().getName()))));
            }
            return true;
        }

        ItemStack itemsToSell = cmd.getPlayer().getItemInHand().clone();
        if (itemsToSell == null || itemsToSell.getType().equals(Material.AIR)) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_HELDITEM);
            return true;
        }

        BaxShop shop = cmd.getShop();
        BaxEntry entry = shop.findEntry(itemsToSell.getType(), itemsToSell.getDurability());
        if (entry == null || entry.refundPrice < 0) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return true;
        }
        
        if (cmd.getArgs().length > 1) {
            int actualAmt;
            try {
                int desiredAmt = Integer.parseInt(cmd.getArgs()[1]);
                actualAmt = clearItems(cmd.getPlayer().getInventory(), entry, desiredAmt);
                if (actualAmt < desiredAmt) {
                cmd.getPlayer().sendMessage(String.format(
                        "You did not have enough to sell §c%d %s§f, so only §a%d§f will be sold.",
                        desiredAmt, desiredAmt == 1 ? "item" : "items",
                        actualAmt));
                }
            } 
            catch (NumberFormatException e) {
                if (cmd.getArgs()[1].equalsIgnoreCase("all")) {
                    actualAmt = clearItems(cmd.getPlayer().getInventory(), entry);
                }
                else if (cmd.getArgs()[1].equalsIgnoreCase("most")) {
                    actualAmt = clearItems(cmd.getPlayer().getInventory(), entry) - 1;
                    ItemStack inHand = entry.toItemStack();
                    inHand.setAmount(1);
                    cmd.getPlayer().setItemInHand(inHand);
                    itemsToSell.setAmount(actualAmt);
                    sell(cmd, itemsToSell);
                    return true;
                }
                else {
                    sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "amount"));
                    return true;
                }
            }
            itemsToSell.setAmount(actualAmt);
        }
        sell(cmd, itemsToSell);
        cmd.getPlayer().setItemInHand(null);
        return true;
    }
    
    private static double sell(ShopCmd cmd, ItemStack itemsToSell) {
        return sell(cmd, itemsToSell, true);
    }
    
    private static double sell(ShopCmd cmd, ItemStack itemsToSell, boolean showExtra) {
        BaxShop shop = cmd.getShop();
        BaxEntry entry = shop.findEntry(itemsToSell.getType(), itemsToSell.getDurability());
        if (entry == null || entry.refundPrice < 0) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return -1.0;
        }
        
        String name = ItemNames.getItemName(itemsToSell);
        
        BaxEntry req = new BaxEntry();
        req.setItem(itemsToSell);
        req.refundPrice = entry.refundPrice;
        
        SellRequest request = new SellRequest(shop, req, cmd.getPlayer().getName());
        
        double price = Main.roundTwoPlaces((double)itemsToSell.getAmount() * entry.refundPrice);
        
        if (shop.sellRequests) {
            cmd.getMain().state.sendNotification(shop.owner, request);
            cmd.getPlayer().sendMessage(String.format(
                "§FYour request to sell §e%d %s§F for §a$%.2f§F has been sent.",
                itemsToSell.getAmount(), name, price));
            if (showExtra) {
                cmd.getPlayer().sendMessage(String.format("§FThis request will expire in %d days.", Resources.EXPIRE_TIME_DAYS));
            }
        }
        else {
            if (request.autoAccept()) {
                cmd.getPlayer().sendMessage(String.format(
                      "§FYou have sold §e%d %s§F for §a$%.2f§F to §1%s§F.",
                      itemsToSell.getAmount(), name, price, shop.owner));
                if (showExtra) {
                    cmd.getPlayer().sendMessage(String.format(Resources.CURRENT_BALANCE, Main.econ.format(Main.econ.getBalance(cmd.getPlayer().getName()))));
                }
                return price;
            }
            else {
                cmd.getMain().state.sendNotification(shop.owner, request);
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
    
    public static boolean remove(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!cmd.getSelection().isOwner && !cmd.getPlayer().hasPermission("shops.admin")) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        if (cmd.getArgs().length < 2) {
            sendError(cmd.getPlayer(), Help.remove.toUsageString());
            return true;
        }

        BaxShop shop = cmd.getShop();
        BaxEntry entry;
        try {
            int index = Integer.parseInt(cmd.getArgs()[1]);
            entry = shop.getEntryAt(index - 1);
        }
        catch (NumberFormatException e) {
            Long item = ItemNames.getItemFromAlias(cmd.getArgs()[1]);
            if (item == null) {
                sendError(cmd.getPlayer(), Resources.NOT_FOUND_ALIAS);
                return true;
            }
            int id = (int) (item >> 16);
            int damage = (short) (item & 0xFFFF);
            entry = shop.findEntry(Material.getMaterial(id), damage);
        } 
        catch (IndexOutOfBoundsException e) {
            entry = null;
        }
        if (entry == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return true;
        }
        
        int amt = entry.getAmount();
        if (cmd.getArgs().length > 2) {
            try {
                amt = getAmount(cmd.getArgs()[2], entry);
                if (!shop.infinite && amt > entry.getAmount()) {
                    sendError(cmd.getPlayer(), Resources.NO_SUPPLIES);
                    return true;
                }
            }
            catch (NumberFormatException e) {
                sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "amount"));
                return true;
            }
        }
        
        ItemStack stack = entry.toItemStack();
        stack.setAmount(amt);
        
        if (amt > 0) {
            if (Main.inventoryFitsItem(cmd.getPlayer(), stack)) {
                cmd.getPlayer().getInventory().addItem(stack);
            }
            else {
                sendError(cmd.getPlayer(), Resources.NO_ROOM);
                return true;
            }
        }
        
        entry.setAmount(entry.getAmount() - amt);
        
        cmd.getMain().sendInfo(cmd.getPlayer(), String.format("§a%d %s§F %s added to your inventory.",
                    amt, ItemNames.getItemName(entry), amt == 1 ? "was" : "were"));
        
        if (entry.getAmount() == 0) {
            shop.inventory.remove(entry);
            cmd.getMain().sendInfo(cmd.getPlayer(), "§fThe shop entry was removed.");
        }
        
        return true;
    }
    
    public static boolean sign(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!cmd.getPlayer().hasPermission("shops.admin") && !cmd.getSelection().isOwner) {
                sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
                return true;
        }
        Block b = cmd.getSelection().location.getBlock();
        if (!b.getType().equals(Material.SIGN) && !b.getType().equals(Material.SIGN_POST)) {
            cmd.getLogger().warning(String.format(Resources.NOT_FOUND_SIGN, cmd.getShop().owner));
            return true;
        }

        Sign sign = (Sign) b.getState();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < cmd.getArgs().length; ++i) {
            sb.append(cmd.getArgs()[i]);
            sb.append(" ");
        }
        int len = sb.length();
        if (len > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        if (len > 60) {
            sendError(cmd.getPlayer(), "That text will not fit on the sign.");
            return true;
        }
        String[] lines = sb.toString().split("\\|");
        for (int i = 0; i < lines.length; ++i) {
            if (lines[i].length() > 15) {
                sendError(cmd.getPlayer(), String.format("Line %d is too long. Lines can only be 15 characters in length.", i + 1));
                return true;
            }
        }
        if (lines.length < 3) {
            sign.setLine(0, "");
            sign.setLine(1, lines[0]);
            sign.setLine(2, lines.length > 1 ? lines[1] : "");
            sign.setLine(3, "");
        } else {
            sign.setLine(0, lines[0]);
            sign.setLine(1, lines.length > 1 ? lines[1] : "");
            sign.setLine(2, lines.length > 2 ? lines[2] : "");
            sign.setLine(3, lines.length > 3 ? lines[3] : "");
        }
        sign.update();
        return true;
    }
    
    public static boolean setindex(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        
        if (!cmd.getPlayer().hasPermission("shops.admin") && !cmd.getSelection().isOwner) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        if (cmd.getArgs().length < 3) {
            sendError(cmd.getSender(), "Usage:\n/shop setindex [current index] [new index]");
        }
        else {
            int oldIndex;
            try {
                oldIndex = Integer.parseInt(cmd.getArgs()[1]);
            }
            catch(NumberFormatException e) {
                Long item = ItemNames.getItemFromAlias(cmd.getArgs()[1]);
                if (item == null) {
                    sendError(cmd.getPlayer(), Resources.NOT_FOUND_ALIAS);
                    return true;
                }
                int id = (int) (item >> 16);
                short damage = (short) (item & 0xFFFF);
                oldIndex = cmd.getShop().getIndexOfEntry(Material.getMaterial(id), damage) + 1;
            } 
            catch (IndexOutOfBoundsException e) {
                oldIndex = -1;
            }
            if (oldIndex < 1 || oldIndex > cmd.getShop().inventory.size()) {
                sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
                return true;
            }
            int newIndex;
            try {
                newIndex = Integer.parseInt(cmd.getArgs()[2]);
            }
            catch(NumberFormatException e) {
                sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "new index"));
                return true;
            }
            if (newIndex > cmd.getShop().inventory.size()) {
                sendError(cmd.getPlayer(), "You must choose a new index that is less than the number of items in the shop!");
                return true;
            }
            if (newIndex < 1) {
                sendError(cmd.getPlayer(), "The new index must be greater than 0.");
                return true;
            }
            if (newIndex == oldIndex) {
                sendError(cmd.getPlayer(), "The index has not been changed.");
                return true;
            }
            BaxEntry entry = cmd.getShop().inventory.remove(oldIndex - 1);
            if (cmd.getShop().inventory.size() < newIndex) {
                cmd.getShop().inventory.add(entry);
            }
            else {
                cmd.getShop().inventory.add(newIndex - 1, entry);
            }
            cmd.getPlayer().sendMessage("§fThe index for this item was successfully changed.");
        }
        return true;
    }
}
