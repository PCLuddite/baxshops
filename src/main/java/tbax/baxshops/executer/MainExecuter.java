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

import tbax.baxshops.CommandHelp;
import tbax.baxshops.Format;
import tbax.baxshops.Help;
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
