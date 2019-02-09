/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;

public final class CmdHelp extends BaxShopCommand
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
    public CommandHelp getHelp(ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("show help with shops");
        help.setArgs(
            new CommandHelpArgument("action", "get help on a /shop action, e.g. /shop h create", true)
        );
        return help;
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
            BaxShopCommand cmd = ShopPlugin.getCommands().get(actor.getArg(1));
            if (cmd == null) {
                actor.exitError(Resources.INVALID_SHOP_ACTION, actor.getArg(1));
            }
            CommandHelp help = cmd.getHelp(actor);
            //noinspection ConstantConditions
            if (cmd == null) {
                actor.exitWarning("No documentation was found for this command.");
            }
            else if (!cmd.hasPermission(actor)) {
                actor.sendError("You do not have permission to view the documentation for this command");
            }
            actor.getSender().sendMessage(help.toString());
        }
    }
}
