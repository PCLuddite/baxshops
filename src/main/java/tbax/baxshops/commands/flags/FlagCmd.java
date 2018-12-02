/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.commands.flags;

import tbax.baxshops.commands.BaxShopCommand;
import tbax.baxshops.commands.ShopCmdActor;

public abstract class FlagCmd extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return getAliases()[0];
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 3;
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public String[] getAliases()
    {
        return null;
    }

    @Override
    public boolean requiresSelection(ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(ShopCmdActor actor)
    {
        return true;
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
}