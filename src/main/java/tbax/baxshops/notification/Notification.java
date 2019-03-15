/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

public interface Notification extends ConfigurationSerializable
{
    @NotNull String getMessage(CommandSender sender);
    @NotNull String getMessage();
}
