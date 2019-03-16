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
import tbax.baxshops.Resources;
import tbax.baxshops.commands.flags.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.StoredPlayer;

public final class CmdFlag extends BaxShopCommand
{
    @SuppressWarnings({"unchecked", "MismatchedQueryAndUpdateOfCollection"})
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
        if (actor.getNumArgs() < 2)
            return false;
        BaxShopCommand command = flagCmds.get(actor.getArg(1));
        return command != null && command.hasValidArgCount(actor);
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        if (actor.getNumArgs() < 2)
            return false;
        BaxShopCommand command = flagCmds.get(actor.getArg(1));
        return command != null && command.requiresSelection(actor);
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        if (actor.getNumArgs() < 2)
            return false;
        BaxShopCommand command = flagCmds.get(actor.getArg(1));
        return command != null && command.requiresOwner(actor);
    }

    @Override
    public boolean requiresPlayer(@NotNull ShopCmdActor actor)
    {
        if (actor.getNumArgs() < 2)
            return false;
        BaxShopCommand command = flagCmds.get(actor.getArg(1));
        return command != null && command.requiresPlayer(actor);
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        if (actor.getNumArgs() < 2)
            return false;
        BaxShopCommand command = flagCmds.get(actor.getArg(1));
        return command != null && command.requiresItemInHand(actor);
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
		FlagCmd flagCmd = (FlagCmd)flagCmds.get(actor.getArg(1));
		if (flagCmd.requiresRealOwner(actor) && actor.getShop() != null && StoredPlayer.DUMMY.equals(actor.getShop().getOwner())) {
		    actor.exitError(Resources.PLAYER_NO_NOTES, actor.getShop().getOwner());
        }
		flagCmd.onCommand(actor);
    }
}
