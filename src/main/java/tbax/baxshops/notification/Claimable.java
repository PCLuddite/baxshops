/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import tbax.baxshops.BaxEntry;
import tbax.baxshops.commands.ShopCmdActor;

public interface Claimable extends Notification
{
    BaxEntry getEntry();
    boolean claim(ShopCmdActor actor);
}
