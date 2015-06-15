/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tbax.baxshops.executer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.Help;
import tbax.baxshops.Main;
import static tbax.baxshops.Main.sendError;
import tbax.baxshops.Resources;
import tbax.baxshops.serialization.StateFile;

/**
 *
 * @author Timothy
 */
public class MainExecuter extends CommandExecuter {

    public MainExecuter(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }

    public boolean execute(String cmd, Main main) {
        switch (cmd.toLowerCase()) {
            case "save":
                return save(main.state);
            case "backup":
                return backup(main.state);
            case "help":
            case "h":
                return help();
        }
        return false;
    }

    public boolean save(StateFile res) {
        if (sender.hasPermission("shops.admin")) {
            res.saveAll();
            sender.sendMessage("§bShops successfully saved");
        } 
        else {
            sendError(sender, Resources.NO_PERMISSION);
        }
        return true;
    }

    public boolean backup(StateFile res) {
        if (sender.hasPermission("shops.admin")) {
            res.backup();
            sender.sendMessage("§bShops successfully backed up state.dat");
        } 
        else {
            sendError(sender, Resources.NO_PERMISSION);
        }
        return true;
    }

    public boolean help() {
        if (args.length > 1) {
            String helpCmd = args[1];
            CommandHelp h = Help.getHelpFor(helpCmd);
            if (h == null) {
                sendError(pl, String.format(Resources.INVALID_SHOP_ACTION, helpCmd));
                return true;
            }
            sender.sendMessage(h.toHelpString());
        } 
        else {
            sender.sendMessage("§bUse this to lookup information on specific commands.");
            sender.sendMessage("§eTo lookup a command, use:\n/shop help <command>");
        }
        return true;
    }
}
