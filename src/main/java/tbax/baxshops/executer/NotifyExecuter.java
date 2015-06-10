/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tbax.baxshops.executer;

import java.util.ArrayDeque;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tbax.baxshops.Help;
import tbax.baxshops.Main;
import static tbax.baxshops.Main.sendError;
import tbax.baxshops.Resources;
import tbax.baxshops.notification.Claimable;
import tbax.baxshops.notification.LollipopNotification;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.notification.Request;
import tbax.baxshops.serialization.ItemNames;

/**
 *
 * @author Timothy
 */
public class NotifyExecuter extends CommandExecuter {

    public NotifyExecuter(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }
    
    @Override
    public boolean execute(String cmd, Main main) {
        switch(cmd.toLowerCase()) {
            case "pending":
            case "p":
            case "notifications":
            case "n":
                return notifications(main);
            case "accept":
            case "yes":
            case "a":
            case "claim":
            case "c":
                return accept(main);
            case "reject":
            case "no":
                return reject(main);
            case "skip":
            case "sk":
                return skip(main);
            case "lookup":
                return lookup(main);
            case "lollipop":
            case "lol":
                return lollipop(main);
        }
        return false;
    }
    
    public boolean notifications(Main main) {
        if (args.length == 1) {
            main.state.showNotification(pl);
        }
        else if (args.length == 2 && args[1].equalsIgnoreCase("clear")) {
            if (pl.hasPermission("shops.admin")) {
                ArrayDeque<Notification> notes = main.state.getNotifications(pl);
                notes.clear();
                pl.sendMessage("§fYour notifications have been cleared");
            }
            else {
                sendError(pl, Resources.NO_PERMISSION);
            }
        }
        return true;
    }
    
    public boolean accept(Main main) {
        ArrayDeque<Notification> notifications = main.state.getNotifications(pl);
        if (notifications.isEmpty()) {
            sendError(pl, Resources.NOT_FOUND_NOTE);
            return true;
        }
        Notification n = notifications.getFirst();
        if (n instanceof Request) {
            Request r = (Request) n;
            if (r.accept(pl)) {
                notifications.removeFirst();
            }
        }
        else if (n instanceof Claimable) {
            Claimable c = (Claimable) n;
            if (c.claim(pl)) {
                notifications.removeFirst();
            }
        }

        main.state.showNotification(pl);
        return true;
    }
    
    public boolean reject(Main main) {
        ArrayDeque<Notification> notifications = main.state.getNotifications(pl);
        if (notifications.isEmpty()) {
            sendError(pl, Resources.NOT_FOUND_NOTE);
            return true;
        }
        Notification n = notifications.getFirst();
        if (n instanceof Request) {
            Request r = (Request) n;
            if (r.reject(pl)) {
                notifications.removeFirst();
            }
        }

        main.state.showNotification(pl);
        return true;
    }
    
    public boolean skip(Main main) {
        ArrayDeque<Notification> notifications = main.state.getNotifications(pl);
        if (notifications.isEmpty()) {
                sendError(pl, Resources.NOT_FOUND_NOTE);
                return true;
        }
        notifications.add(notifications.removeFirst());
        main.state.showNotification(pl);
        return true;
    }
    
    public boolean lookup(Main main) {
        if (args.length < 2) {
            sendError(pl, Help.lookup.toUsageString());
            return true;
        }
        Long alias = ItemNames.getItemFromAlias(args[1]);
        if (alias == null) {
            sendError(pl, Resources.NOT_FOUND_ALIAS);
            return true;
        }
        int id = (int) (alias >> 16);
        int damage = (int) (alias & 0xFFFF);
        sender.sendMessage(String.format("%s is an alias for %d:%d", args[1], id, damage));
        return true;
    }
    
    public boolean lollipop(Main main) {
        double tastiness = LollipopNotification.DEFAULT_TASTINESS;
        if (args.length > 1) {
            if (args.length > 2) {
                try {
                    tastiness = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    sendError(pl, Resources.INVALID_TASTINESS);
                    return true;
                }
            }
            main.state.sendNotification(args[1], new LollipopNotification(pl.getName(), tastiness));
            return true;
        }
        for (Player p : main.getServer().getOnlinePlayers()) {
            main.state.sendNotification(p, new LollipopNotification(pl.getName(), tastiness));
        }
        return true;
    }
}
