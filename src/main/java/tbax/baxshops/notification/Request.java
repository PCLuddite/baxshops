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
    boolean accept(ShopCmdActor acceptingActor);
    boolean reject(ShopCmdActor rejectingActor);
}
