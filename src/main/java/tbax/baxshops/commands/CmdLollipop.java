/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.Main;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.help.CommandHelp;
import tbax.baxshops.notification.LollipopNotification;

public class CmdLollipop extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "lollipop";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"lollipop","lol","lolly"};
    }

    @Override
    public String getPermission()
    {
        return null;
    }

    @Override
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop lollipop", "lol", "[player [tastiness]]", CommandHelp.args(
                "player", "the player",
                "tastiness", "the tastiness (0-100)"
        ));
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 3 || actor.getNumArgs() == 2;
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
        return false;
    }

    @Override
    public boolean requiresItemInHand(ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        double tastiness = LollipopNotification.DEFAULT_TASTINESS;
        if (actor.getNumArgs() == 3) {
            tastiness = actor.getArgDouble(2, "Invalid tastiness");
        }
        Main.getState().sendNotification(actor.getArg(1), new LollipopNotification(actor.getSender().getName(), tastiness));
    }
}
