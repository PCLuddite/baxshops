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
package tbax.baxshops.serialization;

import java.util.HashMap;
import org.bukkit.entity.Player;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Main;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public class Clipboard {
    
    private static final HashMap<Player, HashMap<String, Integer>> clipboard = new HashMap<>();
    
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
            clipboard.put(pl, new HashMap<String, Integer>());
        }
        if (id == null) {
            id = "DEFAULT";
        }
        clipboard.get(pl).put(id, shop.uid);
    }
    
    public static BaxShop get(Player pl, String id) {
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
