package tbax.baxshops.commands;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.*;
import tbax.baxshops.help.CommandHelp;
import tbax.baxshops.notification.SellRequest;
import tbax.baxshops.serialization.ItemNames;

import java.util.ArrayList;
import java.util.List;

public class CmdSell extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "sell";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"sell","s"};
    }

    @Override
    public String getPermission()
    {
        return "shops.sell";
    }

    @Override
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop sell", "s", "<item> <quantity>", "request to sell an item to this shop",
                CommandHelp.args(
                        "item", "the name of the item",
                        "quantity", "the quantity you wish to sell"
                ));
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
        return false;
    }

    @Override
    public boolean requiresPlayer(ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(ShopCmdActor actor)
    {
        return actor.getNumArgs() < 2 || actor.isArgInt(1) || actor.getArg(1).equalsIgnoreCase("all") || actor.getArg(1).equalsIgnoreCase("most");
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        if (actor.isOwner()) {
            actor.setCmdName("restock");  // if they're the owner, use the restock command
            Main.runCommand(actor);
            return;
        }

        if (actor.getNumArgs() == 1) {
            actor.appendArg(actor.getItemInHand().getAmount());
        }

        if (actor.getArg(1).equalsIgnoreCase("most")) {
            actor.setArg(1, actor.getItemInHand().getAmount() - 1);
        }

        List<ItemStack> items = actor.takeArgFromInventory(actor.getItemInHand(), actor.getArg(1));

        if (items.isEmpty()) {
            actor.exitWarning("You did not have anything to sell at this shop.");
        }

        double total = 0.0;
        for(ItemStack itemStack : items) {
            BaxEntry entry = actor.getShop().findEntry(itemStack);
            if (itemStack.getAmount() > 0 && entry != null) {
                double price = sell(actor, itemStack, false);
                if (price >= 0.0) {
                    total += price;
                }
            }
        }
        if (total > 0.0) {
            actor.sendMessage("You earned %s.", Format.money(total));
        }
        else if (total == 0) {
            actor.sendMessage("Your money will be deposited when the buyer accepts the sale.");
        }
    }

    private static double sell(ShopCmdActor actor, ItemStack itemsToSell, boolean showExtra) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        BaxEntry entry = shop.findEntry(itemsToSell);
        if (entry == null || entry.getRefundPrice() < 0) {
            actor.exitError(Resources.NOT_FOUND_SHOPITEM);
        }

        String name = ItemNames.getName(itemsToSell);
        SellRequest request = new SellRequest(shop.getId(), shop.getOwner(), actor.getPlayer().getName(), entry.clone());

        double price = MathUtil.multiply(itemsToSell.getAmount(), entry.getAmount());

        if (shop.hasFlagSellRequests()) {
            Main.getState().sendNotification(shop.getOwner(), request);
            actor.sendMessage("Your request to sell %s for %s has been sent.",
                Format.itemname(itemsToSell.getAmount(), name),
                Format.money(price)

            );
            if (showExtra) {
                actor.sendMessage("This request will expire in %s days.", Format.number(Resources.EXPIRE_TIME_DAYS));
            }
            return 0;
        }
        else {
            request.autoAccept(actor);
            actor.sendMessage(
                "You have sold %s for %s to %s.",
                Format.itemname(itemsToSell.getAmount(), name),
                Format.money(price),
                Format.username(shop.getOwner())
            );
            if (showExtra) {
                actor.sendMessage(Resources.CURRENT_BALANCE, Format.money2(Main.getEconomy().getBalance(actor.getPlayer())));
            }
            return price;
        }
    }
}
