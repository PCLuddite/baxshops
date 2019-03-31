/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.notification;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.serialization.SafeMap;

public interface UpgradeableNote extends Notification
{
    @Deprecated
    void deserialize30(@NotNull SafeMap map);

    @Deprecated
    default void deserialize40(@NotNull SafeMap map)
    {
        deserialize(map);
    }

    void deserialize(@NotNull SafeMap map);
}
