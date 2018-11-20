package tbax.baxshops.commands;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.ItemNames;

import java.util.List;

public class CmdRestock extends BaxShopCommand
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
        return actor.getNumArgs() < 3 || actor.isArgInt(1) || actor.getArg(1).equalsIgnoreCase("all") || actor.getArg(1).equalsIgnoreCase("most");
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        if (actor.getShop().hasFlagInfinite()) {
            actor.exitError("This shop does not need to be restocked.");
        }

        if (actor.getItemInHand() != null) {
            if (actor.getNumArgs() == 1) {
                actor.appendArg(actor.getItemInHand().getAmount()); // restock all in hand if nothing specified
            } else if ("most".equalsIgnoreCase(actor.getArg(1))) {
                actor.setArg(1, actor.getItemInHand().getAmount() - 1);
            }
        }

        ItemStack stack = actor.getItemInHand();
        BaxEntry entry = null;
        if (stack != null && (entry = actor.getShop().findEntry(stack)) == null && requiresItemInHand(actor)) {
            actor.exitError(Resources.NOT_FOUND_SHOPITEM);
        }

        List<BaxEntry> taken = actor.takeArgFromInventory(actor.getArg(1));

        if (requiresItemInHand(actor)) {
            BaxEntry takenItem = taken.get(0);
            entry.add(takenItem.getAmount());
            if (takenItem.getAmount() < actor.getArgInt(1)) {
                actor.sendMessage("Could only restock with " + ChatColor.RED + "%d %s" + ChatColor.RESET + ". The shop now has %s.",
                                    takenItem.getAmount(), ItemNames.getName(takenItem), Format.number(entry.getAmount())
                );
            }
            else {
                actor.sendMessage("Restocked with %s in hand. The shop now has %s.",
                                Format.itemname(stack.getAmount(), ItemNames.getName(entry)), Format.number(entry.getAmount())
                );
            }
        }
        else {
            if (taken.isEmpty()) {
                actor.sendMessage("You did not have any items that could be restocked at this shop.");
            }
            else {
                for (int i = 0; i < taken.size(); i++) {
                    entry = taken.get(i);
                    BaxEntry shopEntry = actor.getShop().findEntry(entry);
                    if (entry.getAmount() > 0) {
                        entry.add(entry.getAmount());
                        actor.sendMessage("Restocked %s.", Format.itemname(entry.getAmount(), ItemNames.getName(entry)));
                    }
                }
            }
        }
    }
}
