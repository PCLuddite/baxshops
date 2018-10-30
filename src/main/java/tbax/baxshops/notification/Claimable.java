/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import org.bukkit.entity.Player;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;

/**
 * A Claimable represents a notification which must wait for
 * certain conditions to be true before completing an action.
 * When a Claimable notification is sent, the notification is
 * automatically claimed if the user is online.
 */
public abstract class Claimable implements Notification
{
    /**
     * An entry for the offered item
     */
    protected BaxEntry entry;
    
    /**
     * Attempts to claim this notification.
     * @param player the player who is claiming the notification
     * @return true if the notification could be claimed, false otherwise
     */
    public boolean claim(Player player)
    {
        if (Main.tryGiveItem(player, entry.toItemStack())) {
            player.sendMessage(Resources.ITEM_ADDED);
            return true;
        }
        else {
            Main.sendError(player, Resources.NO_ROOM);
            return false;
        }
    }
}
