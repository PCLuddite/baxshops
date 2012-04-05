package qs.swornshop.notification;

import java.util.Date;

/**
 * A TimedNotification is a {@link Notification} which expires.
 * TimedNotifications are automatically rejected when they expire.
 */
public interface TimedNotification {
	/**
	 * Returns the time at which this request expires.
	 * @return the time as returned by {@link Date#getTime()}
	 */
	public long expirationDate();
}
