/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.Main;
import tbax.baxshops.CommandHelp;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public class CmdSave extends BaxShopCommand
{

    @Override
    public String getName()
    {
        return "save";
    }

    @Override
    public String getPermission()
    {
        return "shops.admin";
    }

    @Override
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop save", null, null, "saves all shops");
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(ShopCmdActor actor)
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
    public boolean requiresOwner(ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(ShopCmdActor actor)
    {
        Main.getState().saveAll();
        actor.getSender().sendMessage("Shops successfully saved");
    }
}
