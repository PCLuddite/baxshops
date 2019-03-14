/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.serialization.StoredData;

import java.util.Deque;

public final class CmdNotifications extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "pending";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"pending","p","notifications","n"};
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
        help.setDescription(
            "Shows a list of notifications to sell items to your shops\n" +
            "These can be offers (e.g., someone wishes to sell you an item)\n" +
            "or messages (e.g., an offer was accepted).\n" +
            "Use /shop accept and /shop reject on offers."
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return (actor.isAdmin() && actor.getNumArgs() == 2) || actor.getNumArgs() == 1;
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
        return !(actor.getNumArgs() == 2 && actor.getArg(1).equalsIgnoreCase("clear"));
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull ShopCmdActor actor) {
        if (actor.getNumArgs() == 2 && actor.getArg(1).equalsIgnoreCase("clear"))
            return actor.isAdmin();
        return true;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        if (actor.getNumArgs() == 1) {
            StoredData.showNotification(actor.getPlayer());
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
