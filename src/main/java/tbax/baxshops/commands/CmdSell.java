/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.notification.SaleRequest;
import tbax.baxshops.serialization.ItemNames;

import java.util.List;

public final class CmdSell extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "sell";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"sell","s"};
    }

    @Override
    public String getPermission()
    {
        return "shops.sell";
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("sell an item to a shop or request to sell if sell requests for the shop are active");
        help.setArgs(
            new CommandHelpArgument("quantity", "the quantity to you wish to sell", false)
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
        return false;
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
    public boolean hasAlternative(ShopCmdActor actor)
    {
        return actor.isOwner();
    }

    @Override
    public @NotNull Class<? extends BaxShopCommand> getAlternative()
    {
        return CmdRestock.class;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        if (actor.getNumArgs() == 1) {
            assert actor.getItemInHand() != null;
            actor.appendArg(actor.getItemInHand().getAmount());
        }

        List<BaxEntry> items = actor.takeArgFromInventory(1);

        if (items.isEmpty()) {
            actor.exitWarning("You did not have anything to sell at this shop.");
        }

        double total = 0.0;
        for(BaxEntry entry : items) {
            if (entry.getAmount() > 0) {
                double price = sell(actor, entry);
                if (price >= 0.0) {
                    total += price;
                }
            }
        }
        if (total > 0.0) {
            actor.sendMessage("You earned %s.", Format.money(total));
            actor.sendMessage(Resources.CURRENT_BALANCE, Format.money2(ShopPlugin.getEconomy().getBalance(actor.getPlayer())));
        }
        else if (total == 0) {
            actor.sendMessage("Your money will be deposited when the buyer accepts the sale.");
        }
    }

    private static double sell(ShopCmdActor actor, BaxEntry entry) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        assert shop != null;
        if (entry == null || entry.getRefundPrice() < 0) {
            actor.exitError(Resources.NOT_FOUND_SHOPITEM);
        }

        String name = ItemNames.getName(entry.getItemStack());

        double price = MathUtil.multiply(entry.getAmount(), entry.getRefundPrice());

        if (shop.hasFlagSellRequests()) {
            SaleRequest request = new SaleRequest(shop.getId(), shop.getOwner(), actor.getPlayer(), entry);
            ShopPlugin.sendNotification(shop.getOwner(), request);
            actor.sendMessage("Your request to sell %s for %s has been sent.",
                Format.itemName(entry.getAmount(), name), Format.money(price)
            );
            return 0;
        }
        else {
            PlayerUtil.sellItem(shop, shop.getOwner(), actor.getPlayer(), entry);
            actor.sendMessage(
                "You have sold %s for %s to %s.",
                Format.itemName(entry.getAmount(), name),
                Format.money(price),
                Format.username(shop.getOwner().getName())
            );
            return price;
        }
    }
}
