/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.CommandHelp;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.Resources;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.serialization.StoredData;

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
    public CommandHelp getHelp(ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("skip your most recent notification");
        return help;
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
        ArrayDeque<Notification> notifications = StoredData.getNotifications(actor.getPlayer());
        if (notifications.isEmpty()) {
            actor.exitError(Resources.NOT_FOUND_NOTE);
        }
        else {
            notifications.add(notifications.removeFirst());
            ShopPlugin.showNotification(actor.getPlayer());
        }
    }
}
