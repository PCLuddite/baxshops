/*
 * Copyright (C) 2013-2019 Timothy Baxendale
 * Portions derived from Shops Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package tbax.baxshops.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.notification.BuyNotification;
import tbax.baxshops.notification.BuyRequest;
import tbax.baxshops.serialization.ItemNames;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdBuy extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "buy";
    }

    @Override
    public @NotNull String[] getAliases() {
        return new String[]{"buy","b"};
    }

    @Override
    public String getPermission()
    {
        return "shops.buy";
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
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
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        if (actor.getNumArgs() == 1) {
            return actor.getShop() != null && actor.getShop().size() == 1;
        }
        return actor.getNumArgs() == 2 || actor.getNumArgs() == 3;
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
        return false;
    }

    @Override
    public boolean useAlternative(ShopCmdActor actor)
    {
        return actor.isOwner();
    }

    @Override
    public @NotNull Class<? extends BaxShopCommand> getAlternative()
    {
        return CmdTake.class;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException // tested OK 3/16/19
    {
        if (actor.getNumArgs() == 1) {
            actor.appendArgs(1, 1);
        }
        else if (actor.getNumArgs() == 2) {
            actor.appendArg(1);
        }

        BaxShop shop = actor.getShop();
        assert shop != null;
        BaxEntry entry = actor.getArgEntry(1);
        BaxQuantity amount = actor.getArgShopQty(2, entry);
        if (amount.getQuantity() == 0) {
            actor.exitError("You purchased nothing");
        }
        else if (amount.getQuantity() < 0) {
            actor.exitError(Resources.INVALID_DECIMAL, "amount to buy");
        }
        else if (entry.getAmount() < amount.getQuantity() && !shop.hasFlagInfinite()) {
            actor.exitError(Resources.NO_SUPPLIES);
        }

        String itemName = ItemNames.getName(entry);
        double price = MathUtil.multiply(amount.getQuantity(), entry.getRetailPrice());

        if (!ShopPlugin.getEconomy().has(actor.getPlayer(), price)) {
            actor.exitError(Resources.NO_MONEY_BUYER);
        }

        BaxEntry purchased = new BaxEntry(entry);
        purchased.setAmount(amount.getQuantity());

        if (shop.hasFlagBuyRequests()) {
            if (!shop.hasFlagInfinite()) {
                entry.subtract(amount.getQuantity());
            }

            BuyRequest request = new BuyRequest(shop.getId(), actor.getPlayer(), shop.getOwner(), purchased);
            ShopPlugin.sendNotification(shop.getOwner(), request);
            actor.sendMessage("Your request to buy %s for %s has been sent.", Format.itemName(purchased.getAmount(), itemName), Format.money(price));
        }
        else {
            int overflow = actor.giveItem(purchased.toItemStack());
            if (overflow > 0) {
                price = MathUtil.multiply((amount.getQuantity() - overflow), entry.getRetailPrice());
                actor.sendMessage(Resources.SOME_ROOM + " " + Resources.CHARGED_MSG, amount.getQuantity() - overflow, itemName, Format.money(price));
            }
            else {
                actor.sendMessage("You bought %s for %s.", Format.itemName(purchased.getAmount(), itemName), Format.money(price));
            }
            ShopPlugin.getEconomy().withdrawPlayer(actor.getPlayer(), price);
            if (!shop.hasFlagInfinite()) {
                entry.subtract(amount.getQuantity() - overflow);
            }

            ShopPlugin.getEconomy().depositPlayer(shop.getOwner(), price);

            purchased.subtract(overflow);

            actor.sendMessage(Resources.CURRENT_BALANCE, Format.money2(ShopPlugin.getEconomy().getBalance(actor.getPlayer())));
            if (shop.hasFlagNotify()) {
                ShopPlugin.sendNotification(shop.getOwner(), new BuyNotification(shop.getId(), actor.getPlayer(), shop.getOwner(), purchased));
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        ShopCmdActor actor = (ShopCmdActor)sender;
        if (actor.getShop() != null) {
            if (args.length == 2) {
                return actor.getShop().getAllItemAliases();
            }
            else if (args.length == 3) {
                return Arrays.asList("all", "most", "stack");
            }
        }
        return super.onTabComplete(sender, command, alias, args);
    }
}
