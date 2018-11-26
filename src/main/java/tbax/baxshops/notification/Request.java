/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.notification;

import tbax.baxshops.commands.ShopCmdActor;

public interface Request extends Notification
{
    public boolean accept(ShopCmdActor acceptingActor);
    public boolean reject(ShopCmdActor rejectingActor);
}
