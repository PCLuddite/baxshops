/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.CommandHelp;
import tbax.baxshops.Format;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.errors.PrematureAbortException;

public class CmdHelp extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "help";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"help", "h"};
    }

    @Override
    public String getPermission()
    {
        return null;
    }

    @Override
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop help", "h", "[action]", "show help with shops",
                CommandHelp.arg("action", "get help on a /shop action, e.g. /shop h create"));
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 1 || actor.getNumArgs() == 2;
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
        if (actor.getNumArgs() == 1) {
            actor.appendArg(1); // show page 1 by default
        }
        if (actor.isArgInt(1)) {
            actor.sendMessage("Use this to lookup information on specific commands.");
            actor.sendMessage("To lookup a command, use:\n%s\n", Format.command("/shop help <command>"));
        }
        else {
            BaxShopCommand cmd = Main.getCommands().get(actor.getArg(1));
            if (cmd == null) {
                actor.exitError(Resources.INVALID_SHOP_ACTION, actor.getArg(1));
            }
            CommandHelp help = cmd.getHelp();
            if (cmd == null) {
                actor.exitWarning("No documentation was found for this command.");
            }
            actor.getSender().sendMessage(help.toHelpString());
        }
    }
}
