/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.Resources;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.notification.Request;
import tbax.baxshops.serialization.StoredData;

import java.util.Deque;

public final class CmdReject extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "reject";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"reject","no"};
    }

    @Override
    public String getPermission()
    {
        return null;
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("reject your most recent notification");
        return help;
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
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        Deque<Notification> notifications = actor.getNotifications();
        if (notifications.isEmpty()) {
            actor.exitError(Resources.NOT_FOUND_NOTE);
        }
        else {
            Notification n = notifications.getFirst();
            if (n instanceof Request) {
                Request r = (Request) n;
                if (r.reject(actor)) {
                    notifications.removeFirst();
                }
                ShopPlugin.showNotification(actor.getPlayer());
            }
            else {
                actor.sendError("Your current notification is not a request.");
            }
        }
    }
}
