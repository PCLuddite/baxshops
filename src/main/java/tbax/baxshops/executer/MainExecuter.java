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

import tbax.baxshops.CommandHelp;
import tbax.baxshops.Help;
import static tbax.baxshops.Main.sendError;
import tbax.baxshops.Resources;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public class MainExecuter {

    public static boolean execute(ShopCmd cmd) {
        switch (cmd.getArgs()[0]) {
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
            cmd.getState().saveAll();
            cmd.getSender().sendMessage("§bShops successfully saved");
        } 
        else {
            sendError(cmd.getSender(), Resources.NO_PERMISSION);
        }
        return true;
    }

    public static boolean backup(ShopCmd cmd) {
        if (cmd.getSender().hasPermission("shops.admin")) {
            cmd.getState().backup();
            cmd.getSender().sendMessage("§bShops successfully backed up state.dat");
        } 
        else {
            sendError(cmd.getSender(), Resources.NO_PERMISSION);
        }
        return true;
    }

    public static boolean help(ShopCmd cmd) {
        if (cmd.getArgs().length > 1) {
            String helpCmd = cmd.getArgs()[1];
            CommandHelp h = Help.getHelpFor(helpCmd);
            if (h == null) {
                sendError(cmd.getSender(), String.format(Resources.INVALID_SHOP_ACTION, helpCmd));
                return true;
            }
            cmd.getSender().sendMessage(h.toHelpString());
        } 
        else {
            cmd.getSender().sendMessage("§bUse this to lookup information on specific commands.");
            cmd.getSender().sendMessage("§eTo lookup a command, use:\n/shop help <command>");
        }
        return true;
    }
}
