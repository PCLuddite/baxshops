/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.CommandHelpArgument;
import tbax.baxshops.commands.flags.*;
import tbax.baxshops.errors.PrematureAbortException;

public final class CmdFlag extends BaxShopCommand
{
    @SuppressWarnings("unchecked")
    private static final CommandMap flagCmds = new CommandMap(
        FlagCmdSellToShop.class,
        FlagCmdInfinite.class,
        FlagCmdSellRequests.class,
        FlagCmdBuyRequests.class,
        FlagCmdOwner.class,
        FlagCmdList.class
    );

    @Override
    public @NotNull String getName()
    {
        return "flag";
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("Set a specific flag or list all flags applied to a selected shop");
        help.setArgs(
            new CommandHelpArgument("name|list", "the name of the flag to set or a list of all flags currently applied to this shop", true),
            new CommandHelpArgument("setting", "the value this flag should be set to", false)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() >= 2 && flagCmds.get(actor.getArg(1)).hasValidArgCount(actor);
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() >= 2 && flagCmds.get(actor.getArg(1)).requiresSelection(actor);
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() >= 2 && flagCmds.get(actor.getArg(1)).requiresOwner(actor);
    }

    @Override
    public boolean requiresPlayer(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() >= 2 && flagCmds.get(actor.getArg(1)).requiresPlayer(actor);
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() >= 2 && flagCmds.get(actor.getArg(1)).requiresItemInHand(actor);
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
		flagCmds.get(actor.getArg(1)).onCommand(actor);
    }
}
