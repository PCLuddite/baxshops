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
