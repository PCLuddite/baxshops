/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.commands;

import org.bukkit.inventory.ItemStack;
import tbax.baxshops.*;
import tbax.baxshops.help.CommandHelp;
import tbax.baxshops.help.Help;
import tbax.baxshops.serialization.ItemNames;

public class CmdTake extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "take";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"take","t"};
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop take", null, "<name> [amount]", "Takes an item from the shop",
            CommandHelp.args("name", "the name of the item",
                "amount", "the amount to take. Default is 1."));
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        BaxShop shop;
        if (actor.getNumArgs() == 3 || actor.getNumArgs() == 2)
            return true;
        shop = actor.getShop();
        return actor.getNumArgs() == 1 && shop != null && shop.getInventorySize() == 1;
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
        BaxEntry entry;

        if (actor.getNumArgs() == 1) {
            actor.appendArgs(1, 1);
        }
        else if (actor.getNumArgs() == 2) {
            actor.appendArg(1);
        }

        entry = actor.getArgEntry(1);
        int amt = entry.argToAmnt(actor.getArg(1));

        if (!shop.hasFlagInfinite() && amt > entry.getAmount()) {
            actor.exitError(Resources.NO_SUPPLIES);
        }

        ItemStack stack = entry.toItemStack();
        stack.setAmount(amt);

        entry.subtract(amt);

        int overflow = actor.giveItem(stack);
        if (overflow > 0) {
            entry.add(overflow);
            actor.sendMessage(Resources.SOME_ROOM, amt - overflow, ItemNames.getName(stack));
        }
        else {
            actor.sendMessage("%s %s added to your inventory.",Format.itemname(amt, ItemNames.getName(entry)), amt == 1 ? "was" : "were");
        }
    }
}
