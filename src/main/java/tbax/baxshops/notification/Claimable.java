/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.notification;

import tbax.baxshops.BaxEntry;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.errors.PrematureAbortException;

public interface Claimable extends Notification
{
    public BaxEntry getEntry();
    public boolean claim(ShopCmdActor actor);
}
