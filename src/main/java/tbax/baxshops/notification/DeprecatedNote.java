/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.notification;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

@Deprecated
public interface DeprecatedNote extends ConfigurationSerializable
{
    @NotNull Notification getNewNote();
    @NotNull Class<? extends Notification> getNewNoteClass();
}
