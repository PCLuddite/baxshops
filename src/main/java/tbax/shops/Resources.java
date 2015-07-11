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
package tbax.shops;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tbax.shops.notification.Notification;
import tbax.shops.serialization.State2;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public final class Resources {
    	
    // TODO: Timed notifications
    /**
     * The block ID for a signpost
     */
    public static final int SIGN = 63;

    /**
     * The distance from the sign in any direction which the player can go
     * before they leave the shop
     */
    public static final int SHOP_RANGE = 4 * 4;
    
    /**
     * A lookup table for aliases. Aliases are stored as
     * <code>alias =&gt; (ID &lt;&lt; 16) | (damageValue)</code>
     */
    public static HashMap<String, Long> aliases = new HashMap<>();
    /**
     * A lookup table for item names. Item names are stored as
     * <code>(ID &lt;&lt; 16) | (damageValue) =&gt; itemName</code>
     */
    public static HashMap<Long, String> itemNames = new HashMap<>();
    
    public static final String NOT_FOUND_SELECTED = "You do not have any shop selected!\nYou must select a shop to perform this action!";
    public static final String[] SIGN_CLOSED = {"This shop has", "been closed by", "%s"};
    // Errors
    public static final String NO_ROOM = "You have no room in your inventory!";
    public static final String NO_PERMISSION = "You do not have permission to use this command!";
    public static final String CURRENT_BALANCE = "§FYour current balance is §2%s";
    public static final String NOT_FOUND_SHOPITEM = "That item has not been added to this shop.\nUse /shop add to add a new item";
    public static final String INVALID_DECIMAL = "The number entered for the %s is invalid.";
    public static final String NO_MONEY = "You do not have enough money to complete this action.";
    public static final String ERROR_INLINE = "<ERROR>";
    public static final String NOT_FOUND_HELDITEM = "You need to be holding an item to perform this action.";
    public static final String INVALID_SHOP_ACTION = "'/shop %s' is not a valid action";
    // Info
    public static final String SOME_ROOM = "Only §C%d %s§F fit in your inventory. You were charged §2$%.2f§F.";
    public static final String NO_SUPPLIES = "There is not enough of this item in the shop.";
    public static final String ERROR_GENERIC = "Could not complete action because of the following error:\n%s";
    public static final String NOT_FOUND_ALIAS = "The item alias could not be found!";
    public static final String NOT_FOUND_SIGN = "%s's shop is missing its sign!";
    public static final String INVALID_TASTINESS = "Invalid tastiness";
    public static final String NOT_FOUND_NOTE = "You have no notifications for this action.";
    
    public static final int EXPIRE_TIME_DAYS = 5;
    
    public static final String BAK_FILE_PATH = "backups/%d.dat";
    public static final String DAT_FILE_PATH = "shops.dat";
    
    /**
     * A map of shops, accessed by their location in the world
     */
    public HashMap<Location, BaxShop> shops = new HashMap<>();
    /**
     * A map containing each player's currently selected shop and other
     * selection data
     */
    public HashMap<Player, ShopSelection> selectedShops = new HashMap<>();
    /**
     * A map containing each player's current options shop and other
     * option data
     */
    public HashMap<Player, HashMap<String, Object>> tempSettings = new HashMap<>();
    /**
     * A map containing each player's notifications
     */
    public HashMap<String, ArrayDeque<Notification>> pending = new HashMap<>();
    /**
     * The plugin logger
     */
    public Logger log;

    private Main main;
    
    public Resources(Main main) {
        this.main = main;
    }
    
    public void load() {
    }
    
    public static String formatLoc(Location loc) {
        return null;
    }
    
    /**
     * Attempts to find an item which matches the given item name (alias).
     *
     * @param alias the item name
     * @return a Long which contains the item ID and damage value as follows:
     * (id << 16) | (damage)
     */
    public Long getItemFromAlias(String alias) {
        return null;
    }

    /**
     * Gets the name of an item.
     *
     * @param item an item stack
     * @return the item's name
     */
    public String getItemName(ItemStack item) {
        return null;
    }

    /**
     * Gets the name of an item.
     *
     * @param entry the shop entry
     * @return the item's name
     */
    public String getItemName(ShopEntry entry) {
        return null;
    }

    /**
     * Gets the name of an item.
     *
     * @param id the item's id
     * @param damage the item's damage value (durability)
     * @return the item's name
     */
    public String getItemName(int id, int damage) {
        return null;
    }
	
    /**
     * Gets a list of notifications for a player.
     *
     * @param pl the player
     * @return the player's notifications
     */
    public ArrayDeque<Notification> getNotifications(Player pl) {
        return null;
    }
	
    /**
     * Gets a list of notifications for a player.
     *
     * @param player the player
     * @return the player's notifications
     */
    public ArrayDeque<Notification> getNotifications(String player) {
        return null;
    }
    
    /**
     * Show a page of a shop's inventory listing.
     *
     * @param sender the player to which the listing is shown
     * @param selection the player's shop selection
     */
	
    /**
     * Loads the alias map from the aliases.txt resource.
     */
    public void loadAliases() {
    }
	
    /**
     * Loads the item names map from the items.txt resource.
     */
    public void loadItemNames() {
    }
	
    /**
     * Saves all shops
     */
    public void saveAll() {
    }
    
    public ArrayList<Integer> damageableIds;
    
    /**
     * Loads the damageable items list from the damageable.txt resource.
     */
    public void loadDamageable() {
    }
    
    /**
     * Attempts to back up the shops.dat savefile.
     * @return a boolean indicating success
     */
    public boolean backup() {
        return false;
    }

    /**
     * Loads all shops from the shops.dat savefile.
     *
     * @return the saved State
     */
    public State2 reloadAll() {
        return null;
    }
    
    /**
     * Shows a player his/her most recent notification. Also shows the
     * notification count.
     *
     * @param pl the player
     */
    public void showNotification(Player pl) {
    }

    /**
     * Shows a player his/her most recent notification.
     *
     * @param pl the player
     * @param showCount whether the notification count should be shown as well
     */
    public void showNotification(Player pl, boolean showCount) {
    }

    /**
     * Sends a notification to a player.
     *
     * @param pl the player
     * @param n the notification
     */
    public void sendNotification(Player pl, Notification n) {
    }

    /**
     * Sends a notification to a player.
     *
     * @param player the player
     * @param n the notification
     */
    public void sendNotification(String player, Notification n) {
    }
	
    /**
     * Checks whether an item stack will fit in a player's inventory.
     *
     * @param pl the player
     * @param item the item
     * @return whether the item will fit
     */
    public static boolean inventoryFitsItem(Player pl, ItemStack item) {
        return false;
    }
    
    public static boolean giveToPlayer(Player player, ItemStack item) {
        return false;
    }
    
    public void clearTempOpts(Player pl) {
    }
    
    public Object getTempOpt(Player pl, String option) {
        return null;
    }
    
    public void setTempOpt(Player pl, String option, Object obj) {
    }
    
    public boolean getTempOptBool(Player pl, String option) {
        return false;
    }
    
    public void removeSelection(Player pl) {
    }
    
    public static double roundTwoPlaces(double value) {
        return 0;
    }
    
    public static String[] insertFirst(String[] array, String item) {
        return null;
    }
}
