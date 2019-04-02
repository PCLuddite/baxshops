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

import java.util.Date;

public interface Notification extends ConfigurationSerializable
{
    @NotNull String getMessage(CommandSender sender);
    @NotNull String getMessage();
    @Nullable Date getSentDate();
}
