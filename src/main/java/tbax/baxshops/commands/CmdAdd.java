package tbax.baxshops.commands;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.*;
import tbax.baxshops.help.CommandHelp;
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
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop add", "+,ad", "<$buy> [$sell=no]", "add held item to this shop",
                CommandHelp.args(
                        "buy-price", "the price of a single item in the stack",
                        "sell-price", "the selling price of a single item in the stack (by default the item cannot be sold)"
                ));
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
        return false;
    }

    @Override
    public boolean requiresItemInHand(ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        double retailAmount = Math.round(100d * actor.getArgDouble(1, String.format(Resources.INVALID_DECIMAL, "buy price")))/100d,
                refundAmount = -1;

        if (actor.getNumArgs() == 3) {
            refundAmount = Math.round(100d * actor.getArgDouble(2, String.format(Resources.INVALID_DECIMAL, "sell price")))/100d;
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
        newEntry.retailPrice = retailAmount;
        newEntry.refundPrice = refundAmount;
        if (actor.getShop().infinite) {
            newEntry.infinite = true;
        }
        actor.getShop().addEntry(newEntry);
        actor.sendMessage("A new entry for %s was added to the shop.", Format.itemname(newEntry.getAmount(), ItemNames.getName(newEntry)));
        if (!actor.getShop().infinite) {
            actor.getPlayer().setItemInHand(null);
        }
    }
}
