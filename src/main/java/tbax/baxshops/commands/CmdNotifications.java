/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.ShopPlugin;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.serialization.StoredData;

import java.util.Deque;

public class CmdNotifications extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "pending";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"pending","p","notifications","n"};
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
        help.setDescription(
            "Shows a list of notifications to sell items to your shops\n" +
            "These can be offers (e.g., someone wishes to sell you an item)\n" +
            "or messages (e.g., an offer was accepted).\n" +
            "Use /shop accept and /shop reject on offers."
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return (actor.isAdmin() && actor.getNumArgs() == 2) || actor.getNumArgs() == 1;
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
        return !(actor.getNumArgs() == 2 && actor.getArg(1).equalsIgnoreCase("clear"));
    }

    @Override
    public boolean requiresItemInHand(ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean hasPermission(ShopCmdActor actor) {
        if (actor.getNumArgs() == 2 && actor.getArg(1).equalsIgnoreCase("clear"))
            return actor.isAdmin();
        return true;
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        if (actor.getNumArgs() == 1) {
            ShopPlugin.showNotification(actor.getPlayer());
        }
        else if (actor.getNumArgs() == 2) {
            if (actor.getArg(1).equalsIgnoreCase("clear")) {
                Deque<Notification> notes = StoredData.getNotifications(actor.getPlayer());
                notes.clear();
                actor.getPlayer().sendMessage("Your notifications have been cleared");
            }
            else {
                actor.exitError("Unknown notification action %s", actor.getArg(2));
            }
        }
    }
}
