/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.ItemNames;

import java.util.List;

public final class CmdRestock extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "restock";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"restock","r"};
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
        help.setDescription("Restock a shop with the held item, or any item in the player's inventory");
        help.setArgs(
            new CommandHelpArgument("quantity", "the amount to restock", false)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 2 || actor.getNumArgs() == 1;
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
        return actor.getNumArgs() < 3 || actor.isArgQtyNotAny(2);
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        assert actor.getShop() != null;
        if (actor.getShop().hasFlagInfinite()) {
            actor.exitError("This shop does not need to be restocked.");
        }

        if (actor.getItemInHand() != null && actor.getNumArgs() == 1) {
            actor.appendArg(actor.getItemInHand().getAmount()); // restock all in hand if nothing specified
        }

        ItemStack stack = actor.getItemInHand();
        BaxEntry entry = null;
        if (stack != null && (entry = actor.getShop().find(stack)) == null && requiresItemInHand(actor)) {
            actor.exitError(Resources.NOT_FOUND_SHOPITEM);
        }
        assert entry != null;

        BaxQuantity qty = new BaxQuantity(actor.getArg(1));
        List<BaxEntry> taken = actor.takeQtyFromInventory(qty);

        if (requiresItemInHand(actor)) {
            BaxEntry takenItem = taken.get(0);
            entry.add(takenItem.getAmount());
            if (!(qty.isAll() || qty.isMost()) && takenItem.getAmount() < qty.getQuantity()) {
                actor.sendMessage("Could only restock with " + ChatColor.RED + "%d %s" + ChatColor.RESET + ". The shop now has %s.",
                                    takenItem.getAmount(), ItemNames.getName(takenItem), Format.number(entry.getAmount())
                );
            }
            else {
                actor.sendMessage("Restocked with %s in inventory. The shop now has %s.",
                                Format.itemName(takenItem.getAmount(), ItemNames.getName(entry)), Format.number(entry.getAmount())
                );
            }
        }
        else {
            if (taken.isEmpty()) {
                actor.sendMessage("You did not have any items that could be restocked at this shop.");
            }
            else {
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < taken.size(); i++) {
                    entry = taken.get(i);
                    BaxEntry shopEntry = actor.getShop().find(entry);
                    assert shopEntry != null;
                    if (shopEntry.getAmount() > 0) {
                        shopEntry.add(entry.getAmount());
                        actor.sendMessage("Restocked %s.", Format.itemName(entry.getAmount(), ItemNames.getName(entry)));
                    }
                }
            }
        }
    }
}
