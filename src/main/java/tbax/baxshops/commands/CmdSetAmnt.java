/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;

public class CmdSetAmnt extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "setamnt";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"setamnt","setamt"};
    }

    @Override
    public String getPermission()
    {
        return "shops.admin";
    }

    @Override
    public CommandHelp getHelp(ShopCmdActor actor) throws PrematureAbortException
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
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 3;
    }

    @Override
    public boolean requiresSelection(ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(ShopCmdActor actor)
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
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        BaxEntry entry = actor.getArgEntry(1);

        int amnt = actor.getArgInt(2, String.format(Resources.INVALID_DECIMAL, "amount"));
        entry.setAmount(amnt);

        actor.sendMessage("The amount has been set.");
    }
}
