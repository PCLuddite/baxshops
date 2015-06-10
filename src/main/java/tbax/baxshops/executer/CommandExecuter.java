/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tbax.baxshops.executer;

import java.util.HashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Main;

public abstract class CommandExecuter {
    
    protected CommandSender sender;
    protected Command command;
    protected String label;
    protected String[] args;
    protected Player pl;
    
    private static final HashMap<Player, HashMap<String, Integer>> clipboard = new HashMap<>();
    
    public CommandExecuter(CommandSender sender, Command command, String label, String[] args) {
        this.sender = sender;
        this.command = command;
        this.label = label;
        this.args = args;
        if (sender instanceof Player) {
            pl = (Player)sender;
        }
    }
    
    public abstract boolean execute(String cmd, Main main);
    
    protected boolean parseBool(String boolString) {
        if (boolString.equalsIgnoreCase("on")) {
            return true;
        }
        else if (boolString.equalsIgnoreCase("yes")) {
            return true;
        }
        else if (boolString.equals("1")) {
            return true;
        }
        else if (boolString.equalsIgnoreCase("enable")) {
            return true;
        }
        else if (boolString.equalsIgnoreCase("enabled")) {
            return true;
        }
        return Boolean.parseBoolean(boolString);
    }
    
    protected boolean isBool(String testString) {
        switch(testString.toLowerCase()) {
            case "true":
            case "false":
            case "yes":
            case "no":
            case "on":
            case "off":
            case "enable":
            case "enabled":
            case "disable":
            case "disabled":
            case "1":
            case "0":
                return true;
            default:
                return false;
        }
    }
    
    protected void clipboardPut(Player pl, String id, BaxShop shop) {
        if (!clipboard.containsKey(pl)) {
            clipboard.put(pl, new HashMap<String, Integer>());
        }
        if (id == null) {
            id = "DEFAULT";
        }
        clipboard.get(pl).put(id, shop.uid);
    }
    
    protected BaxShop clipboardGet(Player pl, String id) {
        if (clipboard.containsKey(pl)) {
            if (id == null) {
                id = "DEFAULT";
            }
            Integer uid = clipboard.get(pl).get(id);
            if (uid != null) {
                return Main.instance.state.getShop(uid);
            }
        }
        return null;
    }
}
