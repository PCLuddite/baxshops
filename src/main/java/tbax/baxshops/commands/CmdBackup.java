/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.StoredData;

public final class CmdBackup extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "backup";
    }

    @Override
    public String getPermission()
    {
        return "shops.admin";
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("backs up shops");
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresPlayer(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) // tested OK 3-14-19
    {
        if (ShopPlugin.getStoredData().backup()) {
            actor.sendMessage("Shops successfully backed up shops.yml");
        }
        else {
            actor.sendError("Unable to back up shops. Check the log for more details.");
        }
    }
}
