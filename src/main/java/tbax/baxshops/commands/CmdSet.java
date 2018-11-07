/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.commands;

import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.Resources;
import tbax.baxshops.help.CommandHelp;
import tbax.baxshops.serialization.ItemNames;

public class CmdSet extends BaxShopCommand
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
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop set", null, "<item> <$buy> <$sell>", "change an item's price",
                CommandHelp.args(
                        "item", "the ID or name of the item to modify",
                        "buy-price", "the new price of a single item in the stack",
                        "sell-price", "the selling price of a single item in the stack (by default the item cannot be sold)"
                ));
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
            int index = actor.getArgInt(1);
            if (index < shop.getInventorySize()) {
                entry = shop.getEntryAt(index);
            }
        }
        else {
            entry = ItemNames.getItemFromAlias(actor.getArg(1), shop, actor.getSender());
        }

        if (entry == null) {
            actor.sendError(Resources.NOT_FOUND_SHOPITEM);
        }

        double retailAmount = actor.getArgRoundedDouble(2, String.format(Resources.INVALID_DECIMAL, "buy price")),
                refundAmount = actor.getArgRoundedDouble(3, String.format(Resources.INVALID_DECIMAL, "sell price"));

        entry.setRetailPrice(retailAmount);
        entry.setRefundPrice(refundAmount);

        if (shop.hasFlagInfinite()) {
            actor.sendMessage("The price for %s was set.", Format.itemname(ItemNames.getName(entry)));
        }
        else {
            actor.sendMessage("The price for %s was set.", Format.itemname(entry.getAmount(), ItemNames.getName(entry)));
        }
    }
}
