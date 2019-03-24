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
    boolean isLegacy(@NotNull SafeMap map);
    void deserializeLegacy(@NotNull SafeMap map);
    void deserialize(@NotNull SafeMap map);
}
