/* 
 * The MIT License
 *
 * Copyright © 2013-2017 Timothy Baxendale (pcluddite@hotmail.com) and 
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
import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.notification.Claimable;
import tbax.baxshops.notification.LollipopNotification;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.notification.Request;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public final class NotifyExecuter
{
    public static boolean execute(ShopCmd cmd)
    {
        switch (cmd.getAction()) {
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
            /*case "lookup":
                return lookup(cmd);*/
            case "lollipop":
            case "lol":
                return lollipop(cmd);
        }
        return false;
    }
    
    public static boolean notifications(ShopCmd cmd)
    {
        if (cmd.getNumArgs() == 1) {
            Main.getState().showNotification(cmd.getPlayer());
        }
        else if (cmd.getNumArgs() == 2 && cmd.getArg(1).equalsIgnoreCase("clear")) {
            if (cmd.getPlayer().hasPermission("shops.admin")) {
                ArrayDeque<Notification> notes = Main.getState().getNotifications(cmd.getPlayer());
                notes.clear();
                cmd.getPlayer().sendMessage("Your notifications have been cleared");
            }
            else {
                Main.sendError(cmd.getPlayer(), Resources.NO_PERMISSION);
            }
        }
        return true;
    }
    
    public static boolean accept(ShopCmd cmd)
    {
        ArrayDeque<Notification> notifications = Main.getState().getNotifications(cmd.getPlayer());
        if (notifications.isEmpty()) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_NOTE);
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

        Main.getState().showNotification(cmd.getPlayer());
        return true;
    }
    
    public static boolean reject(ShopCmd cmd)
    {
        ArrayDeque<Notification> notifications = Main.getState().getNotifications(cmd.getPlayer());
        if (notifications.isEmpty()) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_NOTE);
            return true;
        }
        Notification n = notifications.getFirst();
        if (n instanceof Request) {
            Request r = (Request) n;
            if (r.reject(cmd.getPlayer())) {
                notifications.removeFirst();
            }
        }

        Main.getState().showNotification(cmd.getPlayer());
        return true;
    }
    
    public static boolean skip(ShopCmd cmd)
    {
        ArrayDeque<Notification> notifications = Main.getState().getNotifications(cmd.getPlayer());
        if (notifications.isEmpty()) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_NOTE);
            return true;
        }
        notifications.add(notifications.removeFirst());
        Main.getState().showNotification(cmd.getPlayer());
        return true;
    }
    
    public static boolean lollipop(ShopCmd cmd)
    {
        double tastiness = LollipopNotification.DEFAULT_TASTINESS;
        if (cmd.getNumArgs() > 1) {
            if (cmd.getNumArgs() > 2) {
                try {
                    tastiness = Double.parseDouble(cmd.getArg(2));
                }
                catch (NumberFormatException e) {
                    Main.sendError(cmd.getPlayer(), "Invalid tastiness");
                    return true;
                }
            }
            Main.getState().sendNotification(cmd.getArg(1), new LollipopNotification(cmd.getPlayer().getName(), tastiness));
            return true;
        }
        for (Player p : cmd.getMain().getServer().getOnlinePlayers()) {
            Main.getState().sendNotification(p, new LollipopNotification(cmd.getPlayer().getName(), tastiness));
        }
        return true;
    }
}
