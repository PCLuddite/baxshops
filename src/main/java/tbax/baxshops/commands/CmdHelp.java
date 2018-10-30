package tbax.baxshops.commands;

import tbax.baxshops.Format;
import tbax.baxshops.Resources;
import tbax.baxshops.help.CommandHelp;
import tbax.baxshops.help.Help;

import static tbax.baxshops.Main.sendError;

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
    public void onCommand(ShopCmdActor actor)
    {
        if (actor.getNumArgs() > 1) {
            String helpCmd = actor.getArg(1);
            CommandHelp h = Help.getHelpFor(helpCmd);
            if (h == null) {
                sendError(actor.getSender(), String.format(Resources.INVALID_SHOP_ACTION, helpCmd));
            }
            else {
                actor.getSender().sendMessage(h.toHelpString());
            }
        }
        else {
            actor.getSender().sendMessage("Use this to lookup information on specific commands.");
            actor.getSender().sendMessage("To lookup a command, use:\n" + Format.command("/shop help <command>"));
        }
    }
}
