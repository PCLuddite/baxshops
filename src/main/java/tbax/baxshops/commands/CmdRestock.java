/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.ItemNames;

import java.util.List;

public final class CmdRestock extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "restock";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"restock","r"};
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
        help.setDescription("Restock a shop with the held item, or any item in the player's inventory");
        help.setArgs(
            new CommandHelpArgument("quantity", "the amount to restock", false)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 2 || actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresPlayer(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() < 2 || actor.isArgQtyNotAny(1);
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException // tested OK 3/16/19
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

        List<BaxEntry> taken = actor.takeArgFromInventory(1);

        if (requiresItemInHand(actor)) {
            BaxQuantity qty = actor.getArgPlayerQty(1);
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
                for (BaxEntry takenEntry : taken) {
                    if (takenEntry.getAmount() > 0) {
                        BaxEntry shopEntry = actor.getShop().find(takenEntry);
                        assert shopEntry != null;
                        shopEntry.add(takenEntry.getAmount());
                        actor.sendMessage("Restocked %s.", Format.itemName(takenEntry.getAmount(), ItemNames.getName(takenEntry)));
                    }
                }
            }
        }
    }
}
