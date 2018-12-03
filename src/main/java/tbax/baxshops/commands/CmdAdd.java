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
import tbax.baxshops.serialization.ItemNames;

public class CmdAdd extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "add";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"add","+","ad"};
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
        help.setDescription("add held item to this shop");
        help.setArgs(
          new CommandHelpArgument("buy price", "the price of a single item in the stack", true),
          new CommandHelpArgument("sell price", "the selling price of a single item in the stack (by default the item cannot be sold)", false)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 2 || actor.getNumArgs() == 3;
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
        return true;
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        double retailAmount = actor.getArgRoundedDouble(1, String.format(Resources.INVALID_DECIMAL, "buy price")),
                refundAmount = -1;

        if (actor.getNumArgs() == 3) {
            refundAmount = actor.getArgRoundedDouble(2, String.format(Resources.INVALID_DECIMAL, "sell price"));
        }

        ItemStack stack = actor.getItemInHand();
        if (BaxShop.isShop(stack)) {
            actor.exitError("You can't add a shop to a shop.");
        }
        if (!actor.isAdmin() && stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()) {
            actor.exitError("You cannot add an item to a shop that has been renamed.");
        }
        if (actor.getShop().containsItem(stack)) {
            actor.exitError(
                    "That item has already been added to this shop\n" +
                            "Use /shop restock to restock"
            );
        }

        BaxEntry newEntry = new BaxEntry();
        newEntry.setItem(stack);
        newEntry.setRetailPrice(retailAmount);
        newEntry.setRefundPrice(refundAmount);
        if (actor.getShop().hasFlagInfinite()) {
            newEntry.setInfinite(true);
        }
        actor.getShop().addEntry(newEntry);
        actor.sendMessage("A new entry for %s was added to the shop.", Format.itemName(newEntry.getAmount(), ItemNames.getName(newEntry)));
        if (!actor.getShop().hasFlagInfinite()) {
            actor.getPlayer().getInventory().setItemInMainHand(null);
        }
    }
}
