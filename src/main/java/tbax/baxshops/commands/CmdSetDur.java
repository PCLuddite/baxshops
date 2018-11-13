/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.commands;

import tbax.baxshops.BaxEntry;
import tbax.baxshops.Resources;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.help.CommandHelp;

public class CmdSetDur extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "setdur";
    }

    @Override
    public String getPermission()
    {
        return "shops.admin";
    }

    @Override
    public CommandHelp getHelp()
    {
        return null;
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 3;
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
        BaxEntry entry = actor.getArgEntry(1);
        short damage = actor.getArgShort(2, String.format(Resources.INVALID_DECIMAL, "damage"));
        entry.setDamagePercent(damage);
        actor.sendMessage("The damage has been set.");
    }
}
