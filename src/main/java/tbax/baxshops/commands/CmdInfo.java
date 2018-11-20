/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.commands;

import tbax.baxshops.BaxEntry;
import tbax.baxshops.CommandHelpArgument;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;

public class CmdInfo extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "info";
    }

    @Override
    public String getPermission()
    {
        return "shops.buy";
    }

    @Override
    public CommandHelp getHelp(ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("Gets extended information about an entry in a shop");
        help.setArgs(
            new CommandHelpArgument("item", "the name or shop index of the entry", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 2;
    }

    @Override
    public boolean requiresSelection(ShopCmdActor actor)
    {
        return true;
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
        BaxEntry entry = actor.getArgEntry(1);
        actor.sendMessage(entry.toString());
    }
}
