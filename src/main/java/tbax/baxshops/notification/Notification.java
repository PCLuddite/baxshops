/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface Notification extends ConfigurationSerializable
{
    String getMessage(CommandSender sender);
}
