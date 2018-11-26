/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.CommandHelp;
import tbax.baxshops.Resources;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.notification.Claimable;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.notification.Request;
import tbax.baxshops.serialization.SavedData;

import java.util.ArrayDeque;

public class CmdAccept extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "accept";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"yes","a"};
    }

    @Override
    public String getPermission()
    {
        return null;
    }

    @Override
    public CommandHelp getHelp(ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("accept your most recent notification");
        return help;
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresOwner(ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresPlayer(ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        ArrayDeque<Notification> notifications = SavedData.getNotifications(actor.getPlayer());
        if (notifications.isEmpty()) {
            actor.exitError(Resources.NOT_FOUND_NOTE);
        }
        else {
            Notification n = notifications.getFirst();
            if (n instanceof Request) {
                Request r = (Request) n;
                if (r.accept(actor)) {
                    notifications.removeFirst();
                }
            } else if (n instanceof Claimable) {
                Claimable c = (Claimable) n;
                if (c.claim(actor)) {
                    notifications.removeFirst();
                }
            }
        }
        SavedData.showNotification(actor.getPlayer());
    }
}
