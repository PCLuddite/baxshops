/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.help.CommandHelp;

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
    public abstract CommandHelp getHelp();
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
	
	public static Map<String, BaxShopCommand> createCommandMap(Class<? extends BaxShopCommand>... classes)
	{
		Map<String, BaxShopCommand> map = new HashMap<String, BaxShopCommand>();
		for(int x = 0; x < classes.length; ++x) {
			try {
				BaxShopCommand cmd = classes[x].newInstance();
				for(String alias : cmd.getAliases()) {
					map.put(alias, cmd);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		return map;
	}
}
