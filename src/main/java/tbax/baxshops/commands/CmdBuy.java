/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.notification.BuyNotification;
import tbax.baxshops.notification.BuyRequest;
import tbax.baxshops.serialization.ItemNames;

public final class CmdBuy extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "buy";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"buy","b"};
    }

    @Override
    public String getPermission()
    {
        return "shops.buy";
    }

    @Override
    public CommandHelp getHelp(ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("buy an item from this shop");
        help.setArgs(
            new CommandHelpArgument("item", "the name of the item or an entry number in the shop.    §LNote:§R enchanted items must be bought with an entry number", true),
            new CommandHelpArgument("quantity", "the quantity you wish to buy", false, 1)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        if (actor.getNumArgs() == 1) {
            return actor.getShop() != null && actor.getShop().getInventorySize() == 1;
        }
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
        return false;
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        if (actor.getNumArgs() == 1) {
            actor.appendArgs(1, 1);
        }
        else if (actor.getNumArgs() == 2) {
            actor.appendArg(1);
        }

        BaxShop shop = actor.getShop();
        BaxEntry entry = shop.getEntry(actor.getArg(1));

        int amount = entry.argToAmnt(actor.getArg(2));
        if (amount == 0) {
            actor.exitError("You purchased nothing.");
        }
        else if (amount < 0) {
            actor.exitError(Resources.INVALID_DECIMAL, "buy amount");
        }
        else if (entry.getAmount() < amount && !shop.hasFlagInfinite()) {
            actor.exitError(Resources.NO_SUPPLIES);
        }

        String itemName = ItemNames.getName(entry);
        double price = MathUtil.multiply(amount, entry.getRetailPrice());

        if (!ShopPlugin.getEconomy().has(actor.getPlayer(), price)) {
            actor.exitError(Resources.NO_MONEY_BUYER);
        }

        BaxEntry purchased = new BaxEntry(entry);
        purchased.setAmount(amount);

        if (shop.hasFlagBuyRequests()) {
            if (!shop.hasFlagInfinite()) {
                entry.subtract(amount);
            }

            BuyRequest request = new BuyRequest(shop.getId(), actor.getPlayer(), shop.getOwner(), purchased);
            ShopPlugin.sendNotification(shop.getOwner(), request);
            actor.sendMessage("Your request to buy %s for %s has been sent.", Format.itemName(purchased.getAmount(), itemName), Format.money(price));
        }
        else {
            int overflow = actor.giveItem(purchased.toItemStack());
            if (overflow > 0) {
                price = MathUtil.multiply((amount - overflow), entry.getRetailPrice());
                actor.sendMessage(Resources.SOME_ROOM + " " + Resources.CHARGED_MSG, amount - overflow, itemName, Format.money(price));
            }
            else {
                actor.sendMessage("You bought %s for %s.", Format.itemName(amount, itemName), Format.money(price));
            }
            ShopPlugin.getEconomy().withdrawPlayer(actor.getPlayer(), price);
            if (!shop.hasFlagInfinite()) {
                entry.subtract(amount - overflow);
            }

            ShopPlugin.getEconomy().depositPlayer(shop.getOwner(), price);

            purchased.subtract(overflow);

            actor.sendMessage(Resources.CURRENT_BALANCE, Format.money2(ShopPlugin.getEconomy().getBalance(actor.getPlayer())));
            if (shop.hasFlagNotify()) {
                ShopPlugin.sendNotification(shop.getOwner(), new BuyNotification(shop.getId(), actor.getPlayer(), shop.getOwner(), purchased));
            }
        }
    }
}
