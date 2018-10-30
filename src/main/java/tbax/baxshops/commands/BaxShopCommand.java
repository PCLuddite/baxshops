/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.help.CommandHelp;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public abstract class BaxShopCommand
{
    public abstract String getName();
    public abstract String getPermission();
    public abstract CommandHelp getHelp();
    public abstract boolean hasValidArgCount(ShopCmdActor actor);
    public abstract boolean requiresSelection(ShopCmdActor actor);
    public abstract boolean requiresOwner(ShopCmdActor actor);
    public abstract boolean requiresPlayer(ShopCmdActor actor);
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
}
