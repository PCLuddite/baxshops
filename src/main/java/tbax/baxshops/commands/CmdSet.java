/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.ItemNames;

public final class CmdSet extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "set";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"set","setprice"};
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public CommandHelp getHelp(ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("change the buy or sell price for a shop item");
        help.setArgs(
            new CommandHelpArgument("item", "the item in the shop", true),
            new CommandHelpArgument("$buy", "the new price for buying a single item", true),
            new CommandHelpArgument("$sell", "the new price for selling a single item. If no price is specified, the item cannot be sold.", false)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 3 || actor.getNumArgs() == 4;
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
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        BaxEntry entry = null;

        if (actor.getNumArgs() == 3) {
            actor.appendArg(-1);
        }

        if (actor.isArgInt(1)) {
            int index = actor.getArgInt(1) - 1;
            if (index < shop.size() && index >= 0) {
                entry = shop.getEntry(index);
            }
        }
        else {
            entry = ItemNames.getItemFromAlias(actor.getArg(1), shop);
        }

        if (entry == null) {
            actor.exitError(Resources.NOT_FOUND_SHOPITEM);
        }

        double retailAmount = actor.getArgRoundedDouble(2, String.format(Resources.INVALID_DECIMAL, "buy price")),
                refundAmount = actor.getArgRoundedDouble(3, String.format(Resources.INVALID_DECIMAL, "sell price"));

        entry.setRetailPrice(retailAmount);
        entry.setRefundPrice(refundAmount);

        if (shop.hasFlagInfinite()) {
            actor.sendMessage("The price for %s was set.", Format.itemName(ItemNames.getName(entry)));
        }
        else {
            actor.sendMessage("The price for %s was set.", Format.itemName(entry.getAmount(), ItemNames.getName(entry)));
        }
    }
}
