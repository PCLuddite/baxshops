/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import org.bukkit.entity.Player;

/**
 * A Request represents a /accept- or /reject-able {@link Notification}.
 */
public interface Request extends Notification
{
    /**
     * Attempts to accept this notification.
     * @param player the player who is accepting the notification
     * @return true if the notification could be accepted, false otherwise
     */
    public boolean accept(Player player);
    /**
     * Attempts to reject this notification.
     * @param player the player who is rejecting the notification
     * @return true if the notification could be rejected, false otherwise
     */
    public boolean reject(Player player);
}
