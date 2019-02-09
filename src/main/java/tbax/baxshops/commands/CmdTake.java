/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.bukkit.inventory.ItemStack;
import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.serialization.ItemNames;

public final class CmdTake extends BaxShopCommand
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
    public CommandHelp getHelp(ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("take an item from a shop without purchasing it");
        help.setArgs(
            new CommandHelpArgument("item", "the item to take from the shop", true),
            new CommandHelpArgument("quantity", "the quantity to take from the shop", false, 1)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        BaxShop shop;
        if (actor.getNumArgs() == 3 || actor.getNumArgs() == 2)
            return true;
        shop = actor.getShop();
        return actor.getNumArgs() == 1 && shop != null && shop.size() == 1;
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
        assert shop != null;
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
            actor.sendMessage("%s %s added to your inventory.",Format.itemName(amt, ItemNames.getName(entry)), amt == 1 ? "was" : "were");
        }
    }
}
