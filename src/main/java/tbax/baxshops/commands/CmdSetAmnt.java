/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;

public final class CmdSetAmnt extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "setamnt";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"setamnt","setamt"};
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
        help.setDescription("set the quantity for an item in a shop");
        help.setArgs(
            new CommandHelpArgument("item", "the item for which to set the quantity", true),
            new CommandHelpArgument("quantity", "the quantity for the item", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 3;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return true;
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
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxEntry entry = actor.getArgEntry(1);

        int amnt = actor.getArgInt(2, String.format(Resources.INVALID_DECIMAL, "amount"));
        entry.setAmount(amnt);

        actor.sendMessage("The amount has been set.");
    }
}
