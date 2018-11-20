/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
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

    public boolean hasPermission(ShopCmdActor actor)
    {
        if (getPermission() == null)
            return true;
        return actor.hasPermission(getPermission());
    }

    public boolean requiresAdmin(ShopCmdActor actor)
    {
        return "shops.admin".equalsIgnoreCase(getPermission());
    }
}
