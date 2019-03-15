/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.commands;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.Resources;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.notification.Claimable;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.serialization.StoredData;

import java.util.Deque;

public class CmdClaim extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "claim";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"claim","c"};
    }

    @Override
    public String getPermission()
    {
        return null;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresPlayer(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException // tested OK 3-14-19
    {
        Deque<Notification> notifications = StoredData.getNotifications(actor.getPlayer());
        if (notifications.isEmpty()) {
            actor.exitError(Resources.NOT_FOUND_NOTE);
        }
        else {
            Notification n = notifications.getFirst();
            if (n instanceof Claimable) {
                Claimable c = (Claimable) n;
                if (c.claim(actor)) {
                    notifications.removeFirst();
                }
                StoredData.showNotification(actor.getPlayer());
            }
            else {
                actor.sendError("Your current notification is not a claim.");
            }
        }
    }
}
