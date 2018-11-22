/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.serialization;

import java.util.HashMap;
import org.bukkit.entity.Player;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Main;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public final class Clipboard
{    
    private static final HashMap<Player, HashMap<String, Long>> clipboard = new HashMap<>();
    
    public static boolean parseBoolean(String boolString) {
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
    
    public static boolean isBoolean(String testString) {
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
    
    public static void put(Player pl, String id, BaxShop shop) {
        if (!clipboard.containsKey(pl)) {
            clipboard.put(pl, new HashMap<String, Long>());
        }
        if (id == null) {
            id = "DEFAULT";
        }
        clipboard.get(pl).put(id, shop.getId());
    }
    
    public static BaxShop get(Player pl, String id) {
        if (clipboard.containsKey(pl)) {
            if (id == null) {
                id = "DEFAULT";
            }
            Long uid = clipboard.get(pl).get(id);
            if (uid != null) {
                return Main.getState().getShop(uid);
            }
        }
        return null;
    }
}
