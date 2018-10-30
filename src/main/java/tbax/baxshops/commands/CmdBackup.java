package tbax.baxshops.commands;

import tbax.baxshops.Main;
import tbax.baxshops.help.CommandHelp;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public class CmdBackup extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "backup";
    }

    @Override
    public String getPermission()
    {
        return "shops.admin";
    }

    @Override
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop backup", null, null, "backs up shops");
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection()
    {
        return false;
    }

    @Override
    public boolean requiresPlayer()
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
        Main.getState().backup();
        actor.getSender().sendMessage("Shops successfully backed up state.json");
    }
}
