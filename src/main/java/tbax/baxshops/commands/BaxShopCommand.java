/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.CommandHelp;
import tbax.baxshops.errors.PrematureAbortException;

public abstract class BaxShopCommand
{
    public abstract String getName();
    public abstract String getPermission();

    public CommandHelp getHelp(ShopCmdActor actor) throws PrematureAbortException
    {
        return new CommandHelp(getName(), getAliases());
    }

    public abstract boolean hasValidArgCount(ShopCmdActor actor);
    public abstract boolean requiresSelection(ShopCmdActor actor);
    public abstract boolean requiresOwner(ShopCmdActor actor);
    public abstract boolean requiresPlayer(ShopCmdActor actor);
    public abstract boolean requiresItemInHand(ShopCmdActor actor);
    public abstract void onCommand(ShopCmdActor actor) throws PrematureAbortException;

    public String[] getAliases()
    {
        return new String[]{getName()};
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasPermission(ShopCmdActor actor)
    {
        if (getPermission() == null)
            return true;
        return actor.hasPermission(getPermission());
    }

    public final boolean requiresAdmin()
    {
        return "shops.admin".equalsIgnoreCase(getPermission());
    }
}
