/* 
 * The MIT License
 *
 * Copyright © 2015 Timothy Baxendale (pcluddite@hotmail.com) and 
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
package tbax.shops.notification;

import java.io.Serializable;

import org.bukkit.entity.Player;

/**
 * The notification interface is used to represent shop-related
 * notifications to players, viewed with /shop p[ending].
 * If this notification can be accepted/rejected, it should 
 * implement the {@link Request} interface.
 * If this notification is a message which must complete an action
 * when certain conditions are true, it should implement the 
 * {@link Claimable} interface.
 * A Notification is regarded as a message and will be deleted
 * once viewed, unless the notification is a Request or Claimable
 */
public interface Notification extends Serializable {
	/**
	 * Gets a short message suitable for this notification.
	 * @param player the player to which the message will be sent (may be null)
	 * @return the notification message
	 */
	public String getMessage(Player player);
}
