/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.commands;

import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Resources;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;

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
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop setamnt", null, "<name> <amount>", "Sets the amount of an item is in the shop",
            CommandHelp.args("name", "the name or index of the item",
                "amount", "the amount to stock"));
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
