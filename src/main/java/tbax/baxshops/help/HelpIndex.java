/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops.help;

import org.bukkit.ChatColor;
import static tbax.baxshops.help.Help.*;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 *         Nathan Dinsmore and Sam Lazarus
 */
public class HelpIndex
{
    /**
     * The general index of commands
     */
    public static final String[] GENERAL = {
        CommandHelp.header("Shop Help"),
        HELP.toIndexString(),
        NOTIFICATIONS.toIndexString(),
        ACCEPT.toIndexString(),
        REJECT.toIndexString(),
        CLAIM.toIndexString(),
        SKIP.toIndexString(),
        FLAG.toIndexString(),
        COPY.toIndexString(),
        LIST.toIndexString()
    };
    /**
     * An index of commands only usable when a shop is selected
     */
    public static final String[] SELECTED = {
        ChatColor.YELLOW + "Left- and right-click to browse this shop's items"
    };
    /**
     * An index of commands only usable by an admin (a player with the
     * shops.admin permission)
     */
    public static final String[] ADMIN = {
        CREATE.toIndexString(),
        SAVE.toIndexString(),
        BACKUP.toIndexString()
    };
    /**
     * An index of commands only usable by an admin (a player with the
     * shops.admin permission) who has selected a shop
     */
    public static final String[] SELECTED_ADMIN = {
        DELETE.toIndexString()
    };
    /**
     * An index of commands only usable by a player who has selected a shop
     * which he/she does not own
     */
    public static final String[] NOT_OWNER = {
        BUY.toIndexString(),
        SELL.toIndexString()
    };
    /**
     * An index of commands only usable by a player who has selected a shop
     * which he/she owns
     */
    public static final String[] OWNER = {
        ADD.toIndexString(),
        RESTOCK.toIndexString(),
        RESTOCKALL.toIndexString(),
        SET.toIndexString(),
        REMOVE.toIndexString(),
        SIGN.toIndexString(),
        FLAG.toIndexString(),
        TAKE.toIndexString(),
        SETANGLE.toIndexString()
    };

}
