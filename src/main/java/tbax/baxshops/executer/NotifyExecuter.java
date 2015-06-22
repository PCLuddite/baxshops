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

import java.util.ArrayDeque;
import org.bukkit.entity.Player;
import tbax.baxshops.Help;
import static tbax.baxshops.Main.sendError;
import tbax.baxshops.Resources;
import tbax.baxshops.notification.Claimable;
import tbax.baxshops.notification.LollipopNotification;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.notification.Request;
import tbax.baxshops.serialization.ItemNames;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public class NotifyExecuter {

    public static boolean execute(ShopCmd cmd) {
        switch (cmd.getArgs()[0]) {
            case "pending":
            case "p":
            case "notifications":
            case "n":
                return notifications(cmd);
            case "accept":
            case "yes":
            case "a":
            case "claim":
            case "c":
                return accept(cmd);
            case "reject":
            case "no":
                return reject(cmd);
            case "skip":
            case "sk":
                return skip(cmd);
            case "lookup":
                return lookup(cmd);
            case "lollipop":
            case "lol":
                return lollipop(cmd);
        }
        return false;
    }
    
    public static boolean notifications(ShopCmd cmd) {
        if (cmd.getArgs().length == 1) {
            cmd.getState().showNotification(cmd.getPlayer());
        }
        else if (cmd.getArgs().length == 2 && cmd.getArgs()[1].equalsIgnoreCase("clear")) {
            if (cmd.getPlayer().hasPermission("shops.admin")) {
                ArrayDeque<Notification> notes = cmd.getState().getNotifications(cmd.getPlayer());
                notes.clear();
                cmd.getPlayer().sendMessage("§fYour notifications have been cleared");
            }
            else {
                sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            }
        }
        return true;
    }
    
    public static boolean accept(ShopCmd cmd) {
        ArrayDeque<Notification> notifications = cmd.getState().getNotifications(cmd.getPlayer());
        if (notifications.isEmpty()) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_NOTE);
            return true;
        }
        Notification n = notifications.getFirst();
        if (n instanceof Request) {
            Request r = (Request) n;
            if (r.accept(cmd.getPlayer())) {
                notifications.removeFirst();
            }
        }
        else if (n instanceof Claimable) {
            Claimable c = (Claimable) n;
            if (c.claim(cmd.getPlayer())) {
                notifications.removeFirst();
            }
        }

        cmd.getState().showNotification(cmd.getPlayer());
        return true;
    }
    
    public static boolean reject(ShopCmd cmd) {
        ArrayDeque<Notification> notifications = cmd.getState().getNotifications(cmd.getPlayer());
        if (notifications.isEmpty()) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_NOTE);
            return true;
        }
        Notification n = notifications.getFirst();
        if (n instanceof Request) {
            Request r = (Request) n;
            if (r.reject(cmd.getPlayer())) {
                notifications.removeFirst();
            }
        }

        cmd.getState().showNotification(cmd.getPlayer());
        return true;
    }
    
    public static boolean skip(ShopCmd cmd) {
        ArrayDeque<Notification> notifications = cmd.getState().getNotifications(cmd.getPlayer());
        if (notifications.isEmpty()) {
                sendError(cmd.getPlayer(), Resources.NOT_FOUND_NOTE);
                return true;
        }
        notifications.add(notifications.removeFirst());
        cmd.getState().showNotification(cmd.getPlayer());
        return true;
    }
    
    public static boolean lookup(ShopCmd cmd) {
        if (cmd.getArgs().length < 2) {
            sendError(cmd.getPlayer(), Help.lookup.toUsageString());
            return true;
        }
        Long alias = ItemNames.getItemFromAlias(cmd.getArgs()[1]);
        if (alias == null) {
            sendError(cmd.getPlayer(), Resources.NOT_FOUND_ALIAS);
            return true;
        }
        int id = (int) (alias >> 16);
        int damage = (int) (alias & 0xFFFF);
        cmd.getSender().sendMessage(String.format("%s is an alias for %d:%d", cmd.getArgs()[1], id, damage));
        return true;
    }
    
    public static boolean lollipop(ShopCmd cmd) {
        double tastiness = LollipopNotification.DEFAULT_TASTINESS;
        if (cmd.getArgs().length > 1) {
            if (cmd.getArgs().length > 2) {
                try {
                    tastiness = Double.parseDouble(cmd.getArgs()[2]);
                } catch (NumberFormatException e) {
                    sendError(cmd.getPlayer(), Resources.INVALID_TASTINESS);
                    return true;
                }
            }
            cmd.getState().sendNotification(cmd.getArgs()[1], new LollipopNotification(cmd.getPlayer().getName(), tastiness));
            return true;
        }
        for (Player p : cmd.getMain().getServer().getOnlinePlayers()) {
            cmd.getState().sendNotification(p, new LollipopNotification(cmd.getPlayer().getName(), tastiness));
        }
        return true;
    }
}
