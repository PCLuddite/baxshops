package qs.swornshop.notification;

import org.bukkit.entity.Player;

/**
 * A Claimable represents a notification which must wait for
 * certain conditions to be true before completing an action.
 * When a Claimable notification is sent, the notification is
 * automatically claimed if the user is online.
 */
public interface Claimable extends Notification {
	/**
	 * Attempts to claim this notification.
	 * @param player the player who is claiming the notification
	 * @return true if the notification could be claimed, false otherwise
	 */
	public boolean claim(Player player);
}
