/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.help.CommandHelp;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.notification.Request;

import java.util.ArrayDeque;

public class CmdReject extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "reject";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"reject","no"};
    }

    @Override
    public String getPermission()
    {
        return null;
    }

    @Override
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop reject", "no", null, "reject your most recent notification");
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
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        ArrayDeque<Notification> notifications = Main.getState().getNotifications(actor.getPlayer());
        if (notifications.isEmpty()) {
            actor.exitError(Resources.NOT_FOUND_NOTE);
        }
        else {
            Notification n = notifications.getFirst();
            if (n instanceof Request) {
                Request r = (Request) n;
                if (r.reject(actor.getPlayer())) {
                    notifications.removeFirst();
                }
            }

            Main.getState().showNotification(actor.getPlayer());
        }
    }
}
