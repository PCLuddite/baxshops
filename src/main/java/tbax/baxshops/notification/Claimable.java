/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import tbax.baxshops.BaxEntry;
import tbax.baxshops.Resources;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.errors.PrematureAbortException;

public interface Claimable extends Notification
{
    BaxEntry getEntry();

    default boolean claim(ShopCmdActor actor)
    {
        BaxEntry entry = getEntry();
        try {
            int overflow = actor.giveItem(entry.toItemStack(), false);
            if (overflow > 0) {
                actor.sendMessage(Resources.SOME_ROOM, entry.getAmount() - overflow, entry.getName());
                entry.setAmount(overflow);
                return false;
            }
            return true;
        }
        catch (PrematureAbortException e) {
            actor.sendMessage(e.getMessage());
            return false;
        }
    }
}
