/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tbax.baxshops.executer;

import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Main;
import static tbax.baxshops.Main.sendError;
import tbax.baxshops.Resources;
import tbax.baxshops.ShopSelection;

/**
 *
 * @author Timothy
 */
public class FlagCmdExecuter extends CommandExecuter {
    
    private ShopSelection selection;
    
    public FlagCmdExecuter(CommandSender sender, Command command, String label, String[] args, ShopSelection sel) {
        super(sender, command, label, args);
        selection = sel;
    }
    
    public boolean execute(String cmd, Main main) {
        if (args.length < 1) {
            return false;
        }
        if (!(args[0].equalsIgnoreCase("flag") || args[0].equalsIgnoreCase("opt") || args[0].equalsIgnoreCase("option"))) {
            return false;
        }
        if (args.length < 2) {
            sendError(pl, "expected /shop " + args[0] + " <option>");
            return true;
        }
        switch(args[1]) {
            case "selltoshop":
            case "sell_to_shop":
                return sellToShop();
            case "infinite":
            case "isinfinite":
                return infinite();
            case "sellrequest":
            case "sellrequests":
            case "sell_request":
            case "sell_requests":
                return sellRequests();
            case "buyrequest":
            case "buyrequests":
            case "buy_request":
            case "buy_requests":
                return buyRequests();
            case "owner":
                return owner();
            case "list":
                return list();
            case "notify":
            case "notifications":
            case "notes":
                return shopNotify();
            default:
                sendError(pl, "invalid shop option '" + args[1] + "'");
                return false;
        }
    }
    
    public boolean sellToShop() {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        BaxShop shop = selection.shop;
        if (!pl.hasPermission("shops.admin") && !selection.isOwner) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        if (args.length < 3 || !isBool(args[2])) {
            Main.sendError(sender, "Usage:\n/shop option sell_to_shop [true|false]");
            return true;
        }
        shop.sellToShop = parseBool(args[2]);
        pl.sendMessage("§ESell to Shop §Fis §a" + (shop.sellToShop ? "enabled" : "disabled"));
        return true;
    }
    
    public boolean shopNotify() {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        BaxShop shop = selection.shop;
        if (!pl.hasPermission("shops.admin") && !selection.isOwner) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        if (args.length < 3 || !isBool(args[2])) {
            Main.sendError(sender, "Usage:\n/shop option notify [true|false]");
            return true;
        }
        shop.notify = parseBool(args[2]);
        pl.sendMessage("§ENotifications §F for this shop are §a" + (shop.sellToShop ? "enabled" : "disabled"));
        return true;
    }
    
    public boolean infinite() {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        BaxShop shop = selection.shop;
        if (!pl.hasPermission("shops.admin") && !selection.isOwner) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        if (args.length < 3 || !isBool(args[2])) {
            Main.sendError(sender, "Usage:\n/shop option infinite [true|false]");
            return true;
        }
        shop.infinite = parseBool(args[2]);
        pl.sendMessage("§EInfinite items §Ffor this shop are §a" + (shop.infinite ? "enabled" : "disabled"));
        return true;
    }
    
    public boolean sellRequests() {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        BaxShop shop = selection.shop;
        if (!pl.hasPermission("shops.admin") && !selection.isOwner) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        if (args.length != 3) {
            Main.sendError(sender, "Usage:\n/shop option SELL_REQUESTS [true|false]");
            return true;
        }
        shop.sellRequests = parseBool(args[2]);
        pl.sendMessage("§ESell requests §Ffor this shop are §a" + (shop.sellRequests ? "enabled" : "disabled"));
        return true;
    }
    
    public boolean buyRequests() {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        BaxShop shop = selection.shop;
        if (!pl.hasPermission("shops.admin") && !selection.isOwner) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        if (args.length < 3 || !isBool(args[2])) {
            Main.sendError(sender, "Usage:\n/shop option BUY_REQUESTS [true|false]");
            return true;
        }
        shop.buyRequests = parseBool(args[2]);
        pl.sendMessage("§EBuy requests §Ffor this shop are §a" + (shop.buyRequests ? "enabled" : "disabled"));
        return true;
    }
    
    public boolean owner() {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        BaxShop shop = selection.shop;
        if (!pl.hasPermission("shops.admin") && !selection.isOwner) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        if (args.length != 3) {
            Main.sendError(sender, "Usage:\n/shop option owner [owner name]");
            return true;
        }
        shop.owner = args[2];
        pl.sendMessage("§E" + shop.owner + "§F is now the owner!" + 
                (selection.isOwner ? "\n§aYou will still be able to edit this shop until you leave or reselect it." : ""));
        return true;
    }
    
    public boolean list() {
        if (selection == null) {
            sendError(pl, Resources.NOT_FOUND_SELECTED);
            return true;
        }
        BaxShop shop = selection.shop;
        if (!pl.hasPermission("shops.admin") && !selection.isOwner) {
            sendError(pl, Resources.NO_PERMISSION);
            return true;
        }
        pl.sendMessage("\n§FFlags currently applied to this shop:");
        pl.sendMessage(String.format("§E%1$s§F: §a%2$s", "INFINITE", shop.infinite ? "On" : "Off"));
        pl.sendMessage(String.format("§E%1$s§F: §a%2$s", "NOTIFY", shop.notify ? "On" : "Off"));
        pl.sendMessage(String.format("§E%1$s§F: §a%2$s", "SELL_TO_SHOP", shop.sellToShop ? "On" : "Off"));
        pl.sendMessage(String.format("§E%1$s§F: §a%2$s", "SELL_REQUESTS", shop.sellRequests ? "On" : "Off"));
        pl.sendMessage(String.format("§E%1$s§F: §a%2$s", "BUY_REQUESTS", shop.buyRequests ? "On" : "Off"));
        return true;
    }
}
