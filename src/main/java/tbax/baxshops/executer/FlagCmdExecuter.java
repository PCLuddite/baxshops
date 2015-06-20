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

import tbax.baxshops.BaxShop;
import tbax.baxshops.Main;
import static tbax.baxshops.Main.sendError;
import tbax.baxshops.Resources;
import tbax.baxshops.serialization.Clipboard;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public class FlagCmdExecuter {
    
    public static boolean execute(ShopCmd cmd) {
        if (cmd.getArgs().length < 1) {
            return false;
        }
        if (!(cmd.getArgs()[0].equalsIgnoreCase("flag") || cmd.getArgs()[0].equalsIgnoreCase("opt") || cmd.getArgs()[0].equalsIgnoreCase("option"))) {
            return false;
        }
        if (cmd.getArgs().length < 2) {
            sendError(cmd.getPlayer(), "expected /shop " + cmd.getArgs()[0] + " <option>");
            return true;
        }
        switch(cmd.getArgs()[1]) {
            case "selltoshop":
            case "sell_to_shop":
                return sellToShop(cmd);
            case "infinite":
            case "isinfinite":
                return infinite(cmd);
            case "sellrequest":
            case "sellrequests":
            case "sell_request":
            case "sell_requests":
                return sellRequests(cmd);
            case "buyrequest":
            case "buyrequests":
            case "buy_request":
            case "buy_requests":
                return buyRequests(cmd);
            case "owner":
                return owner(cmd);
            case "list":
                return list(cmd);
            case "notify":
            case "notifications":
            case "notes":
                return shopNotify(cmd);
            default:
                sendError(cmd.getPlayer(), "invalid shop option '" + cmd.getArgs()[1] + "'");
                return false;
        }
    }
    
    public static boolean sellToShop(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        BaxShop shop = cmd.getShop();
        if (!cmd.getPlayer().hasPermission("shops.admin") && !cmd.getSelection().isOwner) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        if (cmd.getArgs().length < 3 || !Clipboard.parseBoolean(cmd.getArgs()[2])) {
            Main.sendError(cmd.getSender(), "Usage:\n/shop option sell_to_shop [true|false]");
            return true;
        }
        shop.sellToShop = Clipboard.parseBoolean(cmd.getArgs()[2]);
        cmd.getPlayer().sendMessage("§ESell to Shop §Fis §a" + (shop.sellToShop ? "enabled" : "disabled"));
        return true;
    }
    
    public static boolean shopNotify(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        BaxShop shop = cmd.getShop();
        if (!cmd.getPlayer().hasPermission("shops.admin") && !cmd.getSelection().isOwner) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        if (cmd.getArgs().length < 3 || !Clipboard.isBoolean(cmd.getArgs()[2])) {
            Main.sendError(cmd.getSender(), "Usage:\n/shop option notify [true|false]");
            return true;
        }
        shop.notify = Clipboard.parseBoolean(cmd.getArgs()[2]);
        cmd.getPlayer().sendMessage("§ENotifications §F for this shop are §a" + (shop.sellToShop ? "enabled" : "disabled"));
        return true;
    }
    
    public static boolean infinite(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        BaxShop shop = cmd.getShop();
        if (!cmd.getPlayer().hasPermission("shops.admin") && !cmd.getSelection().isOwner) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        if (cmd.getArgs().length < 3 || !Clipboard.isBoolean(cmd.getArgs()[2])) {
            Main.sendError(cmd.getSender(), "Usage:\n/shop option infinite [true|false]");
            return true;
        }
        shop.infinite = Clipboard.parseBoolean(cmd.getArgs()[2]);
        cmd.getPlayer().sendMessage("§EInfinite items §Ffor this shop are §a" + (shop.infinite ? "enabled" : "disabled"));
        return true;
    }
    
    public static boolean sellRequests(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        BaxShop shop = cmd.getShop();
        if (!cmd.getPlayer().hasPermission("shops.admin") && !cmd.getSelection().isOwner) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        if (cmd.getArgs().length != 3) {
            Main.sendError(cmd.getSender(), "Usage:\n/shop option SELL_REQUESTS [true|false]");
            return true;
        }
        shop.sellRequests = Clipboard.parseBoolean(cmd.getArgs()[2]);
        cmd.getPlayer().sendMessage("§ESell requests §Ffor this shop are §a" + (shop.sellRequests ? "enabled" : "disabled"));
        return true;
    }
    
    public static boolean buyRequests(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        BaxShop shop = cmd.getShop();
        if (!cmd.getPlayer().hasPermission("shops.admin") && !cmd.getSelection().isOwner) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        if (cmd.getArgs().length < 3 || !Clipboard.isBoolean(cmd.getArgs()[2])) {
            Main.sendError(cmd.getSender(), "Usage:\n/shop option BUY_REQUESTS [true|false]");
            return true;
        }
        shop.buyRequests = Clipboard.parseBoolean(cmd.getArgs()[2]);
        cmd.getPlayer().sendMessage("§EBuy requests §Ffor this shop are §a" + (shop.buyRequests ? "enabled" : "disabled"));
        return true;
    }
    
    public static boolean owner(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        BaxShop shop = cmd.getShop();
        if (!cmd.getPlayer().hasPermission("shops.admin") && !cmd.getSelection().isOwner) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        if (cmd.getArgs().length != 3) {
            Main.sendError(cmd.getSender(), "Usage:\n/shop option owner [owner name]");
            return true;
        }
        shop.owner = cmd.getArgs()[2];
        cmd.getPlayer().sendMessage("§E" + shop.owner + "§F is now the owner!" + 
                (cmd.getSelection().isOwner ? "\n§aYou will still be able to edit this shop until you leave or reselect it." : ""));
        return true;
    }
    
    public static boolean list(ShopCmd cmd) {
        if (cmd.getSelection() == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_SELECTED);
            return true;
        }
        BaxShop shop = cmd.getShop();
        if (!cmd.getPlayer().hasPermission("shops.admin") && !cmd.getSelection().isOwner) {
            sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            return true;
        }
        cmd.getPlayer().sendMessage("\n§FFlags currently apcmd.getPlayer()ied to this shop:");
        cmd.getPlayer().sendMessage(String.format("§E%1$s§F: §a%2$s", "INFINITE", shop.infinite ? "On" : "Off"));
        cmd.getPlayer().sendMessage(String.format("§E%1$s§F: §a%2$s", "NOTIFY", shop.notify ? "On" : "Off"));
        cmd.getPlayer().sendMessage(String.format("§E%1$s§F: §a%2$s", "SELL_TO_SHOP", shop.sellToShop ? "On" : "Off"));
        cmd.getPlayer().sendMessage(String.format("§E%1$s§F: §a%2$s", "SELL_REQUESTS", shop.sellRequests ? "On" : "Off"));
        cmd.getPlayer().sendMessage(String.format("§E%1$s§F: §a%2$s", "BUY_REQUESTS", shop.buyRequests ? "On" : "Off"));
        return true;
    }
}
