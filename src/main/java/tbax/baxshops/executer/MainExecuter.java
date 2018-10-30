/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops.executer;

import tbax.baxshops.help.CommandHelp;
import tbax.baxshops.Format;
import tbax.baxshops.help.Help;
import tbax.baxshops.Main;
import static tbax.baxshops.Main.sendError;
import tbax.baxshops.Resources;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public final class MainExecuter {

    public static boolean execute(ShopCmd cmd) {
        switch (cmd.getAction()) {
            case "save":
                return save(cmd);
            case "backup":
                return backup(cmd);
            case "help":
            case "h":
                return help(cmd);
        }
        return false;
    }

    public static boolean save(ShopCmd cmd) {
        if (cmd.getSender().hasPermission("shops.admin")) {
            Main.getState().saveAll();
            cmd.getSender().sendMessage("Shops successfully saved");
        } 
        else {
            sendError(cmd.getSender(), Resources.NO_PERMISSION);
        }
        return true;
    }

    public static boolean backup(ShopCmd cmd) {
        if (cmd.getSender().hasPermission("shops.admin")) {
            Main.getState().backup();
            cmd.getSender().sendMessage("Shops successfully backed up state.json");
        } 
        else {
            sendError(cmd.getSender(), Resources.NO_PERMISSION);
        }
        return true;
    }

    public static boolean help(ShopCmd cmd) {
        if (cmd.getNumArgs() > 1) {
            String helpCmd = cmd.getArg(1);
            CommandHelp h = Help.getHelpFor(helpCmd);
            if (h == null) {
                sendError(cmd.getSender(), String.format(Resources.INVALID_SHOP_ACTION, helpCmd));
                return true;
            }
            cmd.getSender().sendMessage(h.toHelpString());
        } 
        else {
            cmd.getSender().sendMessage("Use this to lookup information on specific commands.");
            cmd.getSender().sendMessage("To lookup a command, use:\n" + Format.command("/shop help <command>"));
        }
        return true;
    }
}
