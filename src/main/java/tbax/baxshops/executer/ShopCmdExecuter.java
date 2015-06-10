/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tbax.baxshops.executer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
import tbax.baxshops.ShopSelection;
import tbax.baxshops.notification.BuyNotification;
import tbax.baxshops.notification.BuyRequest;
import tbax.baxshops.notification.SellRequest;
import tbax.baxshops.serialization.ItemNames;

/**
 *
 * @author Timothy
 */
public class ShopCmdExecuter extends CommandExecuter {
    
    ShopSelection selection;
    
    public ShopCmdExecuter(CommandSender sender, Command command, String label, String[] args, ShopSelection sel) {
        super(sender, command, label, args);
        selection = sel;
    }
    
    public boolean execute(String cmd, Main main) {
        switch(cmd.toLowerCase()) {
            case "create":
            case "mk":
                return create(main);
            case "delete":
            case "del":
                return delete(main);
            case "add":
            case "+":
            case "ad":
                return add(selection);
            case "restock":
            case "r":
                return restock(selection);
            case "set":
                return set();
            case "buy":
            case "b":
                return buy(main);
            case "sell":
            case "s":
                return sell(main);
            case "remove":
            case "rm":
                return remove();
            case "sign":
                return sign(main.log, selection);
            case "setindex":
            case "setorder":
            case "reorder":
                return setindex();
            case "setangle":
            case "setface":
            case "face":
                return setangle();
        }
        return false;
    }
    
    public boolean create(Main res) {
        boolean admin = sender.hasPermission("shops.admin");
        if (admin) {
            if (args.length < 2) {
                sendError(pl, Help.create.toUsageString());
                return true;
            }
        }
        
        String owner = admin ? args[1] : pl.getName();
        
        BaxShop shop = new BaxShop();
        shop.addLocation(pl.getLocation().getWorld().getBlockAt(pl.getLocation()).getLocation());
        shop.owner = owner;
        
        if (buildShopSign(pl, new String[] {
            "",
            (owner.length() < 13 ? owner : owner.substring(0, 12) + '…') + "'s",
            "shop",
            ""
        }) == null) {
            return true; // Couldn't build the sign. Retreat!
        }
        
        shop.infinite = admin && args.length > 2 && (args[2].equalsIgnoreCase("yes") || args[2].equalsIgnoreCase("true"));
        shop.sellRequests = !shop.infinite;
        shop.buyRequests = false;
        
        if (!res.state.addShop(pl, shop)) {
            if (!admin) {
                pl.getInventory().addItem(new ItemStack(Material.SIGN)); // give the sign back
            }
            return true;
        }
        pl.sendMessage("§1" + shop.owner + "§F's shop has been created.");
        pl.sendMessage("§EBuy requests §Ffor this shop are §A" + (shop.buyRequests ? "ON" : "OFF"));
        pl.sendMessage("§ESell requests §Ffor this shop are §A" + (shop.sellRequests ? "ON" : "OFF"));
        return true;
    }
    
    public static Block buildShopSign(Player pl, String[] signLines) {
        //Use up a sign if the user is not an admin
        if (!pl.hasPermission("shops.admin") && !pl.hasPermission("shops.owner")) {
            sendError(pl, Resources.NO_PERMISSION);
            return null;
        }
        
        if (!pl.hasPermission("shops.admin")) {
            PlayerInventory inv = pl.getInventory();
            if (!(inv.contains(Material.SIGN))) {
                sendError(pl, "You need a sign to set up a shop.");
                return null;
            }
            inv.remove(new ItemStack(Material.SIGN, 1));
        }
        
        Location loc = pl.getLocation();
        Location locUnder = pl.getLocation();
        locUnder.setY(locUnder.getY() - 1);

        Block b = loc.getWorld().getBlockAt(loc);
        Block blockUnder = locUnder.getWorld().getBlockAt(locUnder);
        if (blockUnder.getType() == Material.AIR ||
            blockUnder.getType() == Material.TNT){
                sendError(pl, "You cannot place a shop on this block.");
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
                sendError(pl, "Unable to place sign! Block type is " + b.getType() + ".");  
                if (!pl.hasPermission("shops.admin")) {
                    pl.getInventory().addItem(new ItemStack(Material.SIGN)); // give the sign back
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
    
    public boolean setangle() {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!pl.hasPermission("shops.admin") && !selection.isOwner) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        if (args.length < 2) {
            sendError(pl, "You must specify a direction to face!");
            return true;
        }
        if (selection.location == null) {
            sendError(pl, "Could not find location");
        }
        else {
            Block b = selection.location.getBlock();
            if (b == null) {
                sendError(pl, "Could not find block");
            }
            else {
                byte angle;
                try {
                    angle = (byte)((Integer.parseInt(args[1]) % 4) << 2);
                }
                catch(NumberFormatException e) {
                    switch(args[1].toLowerCase()) {
                        case "south": angle = 0; break;
                        case "west": angle = 1; break;
                        case "north": angle = 2; break;
                        case "east": angle = 3; break;
                        default:
                            sendError(pl, "The direction you entered wasn't valid! Use one of the four cardinal directions.");
                            return true;
                    }
                    angle = (byte)(angle << 2);
                }
                try {
                    b.setData(angle, false);
                    pl.sendMessage("§fSign rotated to face §e" + args[1].toLowerCase() + "§f");
                }
                catch(Exception e) {
                    sendError(pl, "Some weird error occoured, and long story short, the sign may not have been rotated.");
                }
            }
        }
        return true;
    }
    
    
    
    private boolean checkInventory(Player pl, BaxShop shop) {
        if (shop.inventory == null || shop.inventory.isEmpty()) {
            return true;
        }
        else {
            sendError(pl, "There is still inventory at this shop!");
            sendError(pl, "Remove all inventory before deleting it.");
            return false;
        }
    }
    
    public boolean delete(Main main) {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        
        if (!pl.hasPermission("shops.admin") && !selection.isOwner) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        
        if (args.length == 3) {
            if (args[2].equalsIgnoreCase("all")) {
                if (checkInventory(pl, selection.shop)) {
                    main.state.removeShop(pl, selection.shop);
                }
            }
            else {
                sendError(pl, "invalid argument '" + args[2] + "'");
            }
        }
        else {
            if (selection.shop.getLocations().size() == 1) {
                if (checkInventory(pl, selection.shop)) {
                    main.state.removeShop(pl, selection.shop);
                    main.removeSelection(pl);
                }
            }
            else {
                main.state.removeLocation(pl, selection.location);
            }
        }
        return true;
    }
    
    public boolean add(ShopSelection selection) {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (args.length < 2) {
                sendError(pl, Help.add.toUsageString());
                return true;
        }
        if (!selection.isOwner && !pl.hasPermission("shops.admin")) {
                sendError(pl, Resources.NO_PERMISSION);
                return true;
        }

        double retailAmount, refundAmount;
        try {
                retailAmount = Math.round(100d * Double.parseDouble(args[1])) / 100d;
        } catch (NumberFormatException e) {
                sendError(pl, String.format(Resources.INVALID_DECIMAL, "buy price"));
                sendError(pl, Help.add.toUsageString());
                return true;
        }
        try {
                refundAmount = args.length > 2 ? Math.round(100d * Double.parseDouble(args[2])) / 100d : -1;
        } catch (NumberFormatException e) {
                sendError(pl, String.format(Resources.INVALID_DECIMAL, "sell price"));
                sendError(pl, Help.add.toUsageString());
                return true;
        }
        ItemStack stack = pl.getItemInHand();
        if (stack == null || stack.getType() == Material.AIR) {
                sendError(pl, "You must be holding the item you wisth to add to this shop");
                return true;
        }
        if (selection.shop.containsItem(stack)) {
                sendError(pl, "That item has already been added to this shop");
                sendError(pl, "Use /shop restock to restock");
                return true;
        }
        BaxEntry newEntry = new BaxEntry();
        newEntry.setItem(stack);
        newEntry.retailPrice = retailAmount;
        newEntry.refundPrice = refundAmount;
        if (selection.shop.infinite) {
            newEntry.infinite = true;
        }
        selection.shop.addEntry(newEntry);

        pl.setItemInHand(null);
        return true;
    }
    
    public boolean restock(ShopSelection selection) {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!selection.isOwner && !pl.hasPermission("shops.admin") && selection.shop.infinite) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        if (selection.shop.infinite) {
            sendError(pl, "This shop does not need to be restocked.");
            return true;
        }
        
        if (args.length > 1 && args[1].equalsIgnoreCase("any")) {
            restockAny(pl, selection.shop);
            return true;
        }
        
        ItemStack stack = pl.getItemInHand().clone();
        if (stack == null || stack.getType() == Material.AIR) {
            sendError(pl, Resources.NOT_FOUND_HELDITEM);
            return true;
        }
        
        BaxEntry entry = selection.shop.findEntry(stack);
        if (entry == null) {
            sendError(pl, Resources.NOT_FOUND_SHOPITEM);
            return true;
        }
        
        if (args.length > 1) {
            int amt;
            try {
                amt = Integer.parseInt(args[1]);
            }
            catch(NumberFormatException ex) {
                if (args[1].equalsIgnoreCase("all")) {
                    stack.setAmount(clearItems(pl.getInventory(), entry));
                    entry.add(stack.getAmount());
                    pl.setItemInHand(null);
                }
                else if (args[1].equalsIgnoreCase("most")) {
                    stack.setAmount(clearItems(pl.getInventory(), entry) - 1);
                    entry.add(stack.getAmount());
                    ItemStack inHand = stack.clone();
                    inHand.setAmount(1);
                    pl.setItemInHand(inHand);
                }
                else {
                    sendError(pl, String.format(Resources.INVALID_DECIMAL, "restock amount"));
                    sendError(pl, Help.restock.toUsageString());
                    return true;
                }
                pl.sendMessage(String.format("Restocked with §b%d %s§f. The shop now has §a%d§f.", 
                            stack.getAmount(), stack.getAmount() == 1 ? "item" : "items",
                            entry.getAmount()
                            ));
                return true;
            }
            
            if (pl.getItemInHand() != null && amt < pl.getItemInHand().getAmount()) {
                stack.setAmount(amt);
                pl.getItemInHand().setAmount(pl.getItemInHand().getAmount() - amt); // Don't be hoggin all of it!
            }
            else {
                stack.setAmount(clearItems(pl.getInventory(), entry, amt)); // Ok, take it all
            }
            
            if (stack.getAmount() < amt) {
                entry.add(stack.getAmount());
                pl.setItemInHand(null);
                pl.sendMessage(String.format("Could only restock with §c%d %s§f. You did not have enough to restock §c%d§f. The shop now has §a%d§f.",
                        stack.getAmount(), stack.getAmount() == 1 ? "item" : "items",
                        amt,
                        entry.getAmount()));
                return true;
            }
        }
        else {
            pl.setItemInHand(null);
        }
        
        entry.setAmount(entry.getAmount() + stack.getAmount());
        
        pl.sendMessage(String.format("Restocked with §b%d %s§f in hand. The shop now has §a%d§f.", 
                        stack.getAmount(), stack.getAmount() == 1 ? "item" : "items",
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
    
    public boolean set() {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!selection.isOwner && !pl.hasPermission("shops.admin")) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        
        if (args.length < 3) {
            sendError(pl, Help.set.toUsageString());
            return true;
        }
        BaxShop shop = selection.shop;
        BaxEntry entry;
        try {
            int index = Integer.parseInt(args[1]);
            entry = shop.getEntryAt(index - 1);
        }
        catch (NumberFormatException e) {
            Long item = ItemNames.getItemFromAlias(args[1]);
            if (item == null) {
                sendError(pl, Resources.NOT_FOUND_ALIAS);
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
            sendError(pl, Resources.NOT_FOUND_SHOPITEM);
            return true;
        }

        double retailAmount, refundAmount;
        try {
            retailAmount = Math.round(100d * Double.parseDouble(args[2])) / 100d;
        } 
        catch (NumberFormatException e) {
            sendError(pl, String.format(Resources.INVALID_DECIMAL,  "buy price"));
            sendError(pl, Help.set.toUsageString());
            return true;
        }
        try {
            refundAmount = args.length > 3 ? Math.round(100d * Double.parseDouble(args[3])) / 100d : -1;
        } 
        catch (NumberFormatException e) {
            sendError(pl, String.format(Resources.INVALID_DECIMAL,  "sell price"));
            sendError(pl, Help.set.toUsageString());
            return true;
        }

        entry.retailPrice = retailAmount;
        entry.refundPrice = refundAmount;
        
        return true;
    }
    
    public boolean buy(Main main) {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!pl.hasPermission("shops.buy")) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        if (args.length < 1) {
            sendError(pl, Help.buy.toUsageString());
            return true;
        }
        if (args.length == 1) {
            if (selection.shop.inventory.size() > 1) { // Allow no arguments if there's only one item  
                sendError(pl, Help.buy.toUsageString());
                return true;
            }
            else {
                args = new String[] {
                  args[0], "1" 
                };
            }
        }

        BaxShop shop = selection.shop;
        BaxEntry entry;
        try {
            int index = Integer.parseInt(args[1]);
            entry = shop.getEntryAt(index - 1);
        } 
        catch (NumberFormatException e) {
            Long item = ItemNames.getItemFromAlias(args[1]);
            if (item == null) {
                sendError(pl, Resources.NOT_FOUND_ALIAS);
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
            sendError(pl, Resources.NOT_FOUND_SHOPITEM);
            return true;
        }
        
        int amount;
        if (args.length < 3) {
            amount = 1;
        } 
        else {
            try {
                amount = getAmount(args[2], entry.getItemStack(), entry.infinite);
            }
            catch (NumberFormatException e) {
                sendError(pl, String.format(Resources.INVALID_DECIMAL, "buy amount"));
                sendError(pl, Help.buy.toUsageString());
                return true;
            }
        }
        if (amount == 0) {
            sendError(pl, "Congrats. You bought nothing.");
            return true;
        }
        if (amount < 0) {
            sendError(pl, String.format(Resources.INVALID_DECIMAL, "buy amount"));
            sendError(pl, Help.buy.toUsageString());
            return true;
        }
        
        if (entry.getAmount() < amount && !shop.infinite) {
            sendError(pl, Resources.NO_SUPPLIES);
            return true;
        }
        
        String itemName = ItemNames.getItemName(entry);
        double price = Main.roundTwoPlaces(amount * entry.retailPrice);
        
        if (!econ.has(pl.getName(), price)) {
            sendError(pl, Resources.NO_MONEY);
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
            
            BuyRequest request = new BuyRequest(shop, purchased, pl.getName());
            main.state.sendNotification(shop.owner, request);
            pl.sendMessage(String.format("§FYour request to buy §e%d %s§F for §a$%.2f§F has been sent.",
                           purchased.getAmount(), itemName, price));
            pl.sendMessage(String.format("§FThis request will expire in %d days.", Resources.EXPIRE_TIME_DAYS));
            return true;
        }
        
        HashMap<Integer, ItemStack> overflow = pl.getInventory().addItem(purchased.toItemStack());
        int refunded = 0;
        if (overflow.size() > 0) {
            refunded = overflow.get(0).getAmount();
            if (overflow.size() == amount) {
                sendError(pl, Resources.NO_ROOM);
                return true;
            }
            price = Main.roundTwoPlaces((amount - refunded) * entry.retailPrice);
            sender.sendMessage(String.format(Resources.SOME_ROOM,
                amount - refunded, itemName, price));
        } 
        else {
            sender.sendMessage(String.format("You bought §e%d %s§F for §a$%.2f§F.",
                amount, itemName, price));
        }
        econ.withdrawPlayer(pl.getName(), price);
        if (!shop.infinite) {
            entry.setAmount(entry.getAmount() - (amount - refunded));
        }

        econ.depositPlayer(shop.owner, price);
        
        purchased.setAmount(amount - refunded);
        
        sender.sendMessage(String.format(Resources.CURRENT_BALANCE, Main.econ.format(Main.econ.getBalance(sender.getName()))));
        main.state.sendNotification(shop.owner, new BuyNotification(shop, purchased, pl.getName()));        
        
        return true;
    }
    
    public boolean sell(Main main) {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!pl.hasPermission("shops.sell")) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        if (selection.isOwner && !pl.hasPermission("shops.self")) {
            sendError(pl, "You cannot sell items to yourself.");
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        
        if (args.length > 1 && args[1].equalsIgnoreCase("any")) {
            ArrayList<ItemStack> toSell = clearItems(pl.getInventory(), selection.shop.inventory);
            if (toSell.isEmpty()) {
                sendError(pl, "You did not have any items that could be sold at this shop.");
            }
            else {
                double total = 0.0;
                for(ItemStack itemStack : toSell) {
                    BaxEntry entry = selection.shop.findEntry(itemStack);
                    if (itemStack.getAmount() > 0 && entry != null) {
                        double price = Main.roundTwoPlaces(sell(pl, main, itemStack, false));
                        if (price >= 0.0) {
                            total += price;
                        }
                    }
                }
                if (total > 0.0) {
                    pl.sendMessage(String.format("§fYou earned §a$%.2f§f", total));
                }
                pl.sendMessage(String.format(Resources.CURRENT_BALANCE, Main.econ.format(Main.econ.getBalance(pl.getName()))));
            }
            return true;
        }

        ItemStack itemsToSell = pl.getItemInHand().clone();
        if (itemsToSell == null || itemsToSell.getType().equals(Material.AIR)) {
            sendError(pl, Resources.NOT_FOUND_HELDITEM);
            return true;
        }

        BaxShop shop = selection.shop;
        BaxEntry entry = shop.findEntry(itemsToSell.getType(), itemsToSell.getDurability());
        if (entry == null || entry.refundPrice < 0) {
            sendError(pl, Resources.NOT_FOUND_SHOPITEM);
            return true;
        }
        
        if (args.length > 1) {
            int actualAmt;
            try {
                int desiredAmt = Integer.parseInt(args[1]);
                actualAmt = clearItems(pl.getInventory(), entry, desiredAmt);
                if (actualAmt < desiredAmt) {
                pl.sendMessage(String.format(
                        "You did not have enough to sell §c%d %s§f, so only §a%d§f will be sold.",
                        desiredAmt, desiredAmt == 1 ? "item" : "items",
                        actualAmt));
                }
            } 
            catch (NumberFormatException e) {
                if (args[1].equalsIgnoreCase("all")) {
                    actualAmt = clearItems(pl.getInventory(), entry);
                }
                else if (args[1].equalsIgnoreCase("most")) {
                    actualAmt = clearItems(pl.getInventory(), entry) - 1;
                    ItemStack inHand = entry.toItemStack();
                    inHand.setAmount(1);
                    pl.setItemInHand(inHand);
                    itemsToSell.setAmount(actualAmt);
                    sell(pl, main, itemsToSell);
                    return true;
                }
                else {
                    sendError(pl, String.format(Resources.INVALID_DECIMAL, "amount"));
                    return true;
                }
            }
            itemsToSell.setAmount(actualAmt);
        }
        sell(pl, main, itemsToSell);
        pl.setItemInHand(null);
        return true;
    }
    
    private double sell(Player pl, Main main, ItemStack itemsToSell) {
        return sell(pl, main, itemsToSell, true);
    }
    
    private double sell(Player pl, Main main, ItemStack itemsToSell, boolean showExtra) {
        BaxShop shop = selection.shop;
        BaxEntry entry = shop.findEntry(itemsToSell.getType(), itemsToSell.getDurability());
        if (entry == null || entry.refundPrice < 0) {
            sendError(pl, Resources.NOT_FOUND_SHOPITEM);
            return -1.0;
        }
        
        String name = ItemNames.getItemName(itemsToSell);
        
        BaxEntry req = new BaxEntry();
        req.setItem(itemsToSell);
        req.refundPrice = entry.refundPrice;
        
        SellRequest request = new SellRequest(shop, req, pl.getName());
        
        double price = Main.roundTwoPlaces((double)itemsToSell.getAmount() * entry.refundPrice);
        
        if (shop.sellRequests) {
            main.state.sendNotification(shop.owner, request);
            pl.sendMessage(String.format(
                "§FYour request to sell §e%d %s§F for §a$%.2f§F has been sent.",
                itemsToSell.getAmount(), name, price));
            if (showExtra) {
                pl.sendMessage(String.format("§FThis request will expire in %d days.", Resources.EXPIRE_TIME_DAYS));
            }
        }
        else {
            if (request.autoAccept()) {
                pl.sendMessage(String.format(
                      "§FYou have sold §e%d %s§F for §a$%.2f§F to §1%s§F.",
                      itemsToSell.getAmount(), name, price, shop.owner));
                if (showExtra) {
                    pl.sendMessage(String.format(Resources.CURRENT_BALANCE, Main.econ.format(Main.econ.getBalance(pl.getName()))));
                }
                return price;
            }
            else {
                main.state.sendNotification(shop.owner, request);
                Main.sendError(pl, 
                    String.format("The owner could not purchase %d %s. A request has been sent to the owner to accept your offer at a later time.",
                                  itemsToSell.getAmount(), name)
                );
                if (showExtra) {
                    Main.sendError(pl, String.format("This request will expire in %d days.", Resources.EXPIRE_TIME_DAYS));
                }
            }
        }
        return -1.0;
    }
    
    public boolean remove() {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!selection.isOwner && !pl.hasPermission("shops.admin")) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        if (args.length < 2) {
            sendError(pl, Help.remove.toUsageString());
            return true;
        }

        BaxShop shop = selection.shop;
        BaxEntry entry;
        try {
            int index = Integer.parseInt(args[1]);
            entry = shop.getEntryAt(index - 1);
        }
        catch (NumberFormatException e) {
            Long item = ItemNames.getItemFromAlias(args[1]);
            if (item == null) {
                sendError(pl, Resources.NOT_FOUND_ALIAS);
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
            sendError(pl, Resources.NOT_FOUND_SHOPITEM);
            return true;
        }
        
        int amt = entry.getAmount();
        if (args.length > 2) {
            try {
                amt = getAmount(args[2], entry);
                if (!shop.infinite && amt > entry.getAmount()) {
                    sendError(pl, Resources.NO_SUPPLIES);
                    return true;
                }
            }
            catch (NumberFormatException e) {
                sendError(pl, String.format(Resources.INVALID_DECIMAL, "amount"));
                return true;
            }
        }
        
        ItemStack stack = entry.toItemStack();
        stack.setAmount(amt);
        
        if (amt > 0) {
            if (Main.inventoryFitsItem(pl, stack)) {
                pl.getInventory().addItem(stack);
            }
            else {
                sendError(pl, Resources.NO_ROOM);
                return true;
            }
        }
        
        entry.setAmount(entry.getAmount() - amt);
        
        pl.sendMessage(String.format("§a%d %s§F %s added to your inventory.",
                    amt, ItemNames.getItemName(entry), amt == 1 ? "was" : "were"));
        
        if (entry.getAmount() == 0) {
            shop.inventory.remove(entry);
            pl.sendMessage("§fThe shop entry was removed.");
        }
        
        return true;
    }
    
    public boolean sign(Logger log, ShopSelection selection) {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        if (!pl.hasPermission("shops.admin") && !selection.isOwner) {
                sendError(pl, Resources.NO_PERMISSION);
                return true;
        }
        Block b = selection.location.getBlock();
        if (!b.getType().equals(Material.SIGN) && !b.getType().equals(Material.SIGN_POST)) {
            log.warning(String.format(Resources.NOT_FOUND_SIGN, selection.shop.owner));
            return true;
        }

        Sign sign = (Sign) b.getState();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; ++i) {
            sb.append(args[i]);
            sb.append(" ");
        }
        int len = sb.length();
        if (len > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        if (len > 60) {
            sendError(pl, "That text will not fit on the sign.");
            return true;
        }
        String[] lines = sb.toString().split("\\|");
        for (int i = 0; i < lines.length; ++i)
            if (lines[i].length() > 15) {
                sendError(pl, String.format("Line %d is too long. Lines can only be 15 characters in length.", i + 1));
                return true;
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
    
    public boolean setindex() {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        
        if (!pl.hasPermission("shops.admin") && !selection.isOwner) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        if (args.length < 3) {
            sendError(sender, "Usage:\n/shop setindex [current index] [new index]");
        }
        else {
            int oldIndex;
            try {
                oldIndex = Integer.parseInt(args[1]);
            }
            catch(NumberFormatException e) {
                Long item = ItemNames.getItemFromAlias(args[1]);
                if (item == null) {
                    sendError(pl, Resources.NOT_FOUND_ALIAS);
                    return true;
                }
                int id = (int) (item >> 16);
                short damage = (short) (item & 0xFFFF);
                oldIndex = selection.shop.getIndexOfEntry(Material.getMaterial(id), damage) + 1;
            } 
            catch (IndexOutOfBoundsException e) {
                oldIndex = -1;
            }
            if (oldIndex < 1 || oldIndex > selection.shop.inventory.size()) {
                sendError(pl, Resources.NOT_FOUND_SHOPITEM);
                return true;
            }
            int newIndex;
            try {
                newIndex = Integer.parseInt(args[2]);
            }
            catch(NumberFormatException e) {
                sendError(pl, String.format(Resources.INVALID_DECIMAL, "new index"));
                return true;
            }
            if (newIndex > selection.shop.inventory.size()) {
                sendError(pl, "You must choose a new index that is less than the number of items in the shop!");
                return true;
            }
            if (newIndex < 1) {
                sendError(pl, "The new index must be greater than 0.");
                return true;
            }
            if (newIndex == oldIndex) {
                sendError(pl, "The index has not been changed.");
                return true;
            }
            BaxEntry entry = selection.shop.inventory.remove(oldIndex - 1);
            if (selection.shop.inventory.size() < newIndex) {
                selection.shop.inventory.add(entry);
            }
            else {
                selection.shop.inventory.add(newIndex - 1, entry);
            }
            pl.sendMessage("§fThe index for this item was successfully changed.");
        }
        return true;
    }
}
