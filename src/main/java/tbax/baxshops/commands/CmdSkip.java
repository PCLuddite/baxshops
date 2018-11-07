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

import java.util.ArrayDeque;

public class CmdSkip extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "skip";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"skip","sk"};
    }

    @Override
    public String getPermission()
    {
        return null;
    }

    @Override
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop skip", "sk", null, "skip your most recent notification",
                "Moves your most recent notification to the end of the list");
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return false;
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
        ArrayDeque<Notification> notifications = Main.getState().getNotifications(actor.getPlayer());
        if (notifications.isEmpty()) {
            actor.sendError(Resources.NOT_FOUND_NOTE);
        }
        else {
            notifications.add(notifications.removeFirst());
            Main.getState().showNotification(actor.getPlayer());
        }
    }
}
