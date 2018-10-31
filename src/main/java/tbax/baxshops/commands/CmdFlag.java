/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.bukkit.entity.Player;
import tbax.baxshops.help.CommandHelp;

public class CmdFlag extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "flag";
    }

    @Override
    public String getPermission()
    {
        return null;
    }

    @Override
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop flag", null, "<name|list> [setting]", "Set a specific flag or list all flags applied to a selected shop",
                CommandHelp.args("name", "the name of the flag to set",
                        "setting", "the option to set the flag",
                        "list", "lists all flags applied to the shop"));
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return false;
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

    @Override
    public void onCommand(ShopCmdActor actor)
    {

    }
}
