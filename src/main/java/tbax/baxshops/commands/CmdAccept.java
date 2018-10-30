package tbax.baxshops.commands;

import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.help.CommandHelp;
import tbax.baxshops.notification.Claimable;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.notification.Request;

import java.util.ArrayDeque;

public class CmdAccept extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "accept";
    }

    @Override
    public String getPermission()
    {
        return null;
    }

    @Override
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop accept", "yes,a", null, "accept your most recent notification");
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection()
    {
        return false;
    }

    @Override
    public boolean requiresOwner()
    {
        return false;
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        ArrayDeque<Notification> notifications = Main.getState().getNotifications(actor.getPlayer());
        if (notifications.isEmpty()) {
            actor.sendError(Resources.NOT_FOUND_NOTE);
        }
        else {
            Notification n = notifications.getFirst();
            if (n instanceof Request) {
                Request r = (Request) n;
                if (r.accept(actor.getPlayer())) {
                    notifications.removeFirst();
                }
            } else if (n instanceof Claimable) {
                Claimable c = (Claimable) n;
                if (c.claim(actor.getPlayer())) {
                    notifications.removeFirst();
                }
            }
        }
        Main.getState().showNotification(actor.getPlayer());
    }
}
