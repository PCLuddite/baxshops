package tbax.baxshops.commands;

import org.bukkit.inventory.ItemStack;
import tbax.baxshops.*;
import tbax.baxshops.help.CommandHelp;
import tbax.baxshops.notification.SellRequest;
import tbax.baxshops.serialization.ItemNames;

import java.util.ArrayList;

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

        if (cmd.getNumArgs() > 1 && cmd.getArg(1).equalsIgnoreCase("any")) {
            ArrayList<ItemStack> toSell = Main.clearItems(cmd.getPlayer(), cmd.getShop().inventory);
            if (toSell.isEmpty()) {
                Main.sendError(cmd.getPlayer(), "You did not have any items that could be sold at this shop.");
            }
            else {
                double total = 0.0;
                for(ItemStack itemStack : toSell) {
                    BaxEntry entry = cmd.getShop().findEntry(itemStack);
                    if (itemStack.getAmount() > 0 && entry != null) {
                        double price = Main.roundTwoPlaces(sell(cmd, itemStack, false));
                        if (price >= 0.0) {
                            total += price;
                        }
                    }
                }
                if (total > 0.0) {
                    cmd.getPlayer().sendMessage(String.format("You earned %s.", Format.money(total)));
                }
                cmd.getPlayer().sendMessage(String.format(Resources.CURRENT_BALANCE, Format.money2(Main.getEconomy().getBalance(cmd.getPlayer().getName()))));
            }
            return true;
        }

        ItemStack itemsToSell = cmd.getPlayer().getItemInHand().clone();
        if (itemsToSell == null || itemsToSell.getType().equals(Material.AIR)) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_HELDITEM);
            return true;
        }

        if (cmd.getNumArgs() == 1) {
            cmd.appendArg(String.valueOf(cmd.getPlayer().getItemInHand().getAmount())); // sell in hand if nothing specified
        }

        BaxShop shop = cmd.getShop();
        BaxEntry entry = shop.findEntry(itemsToSell);
        if (entry == null || entry.refundPrice < 0) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return true;
        }

        int actualAmt;
        try {
            int desiredAmt = Integer.parseInt(cmd.getArg(1));
            actualAmt = Main.clearItems(cmd.getPlayer(), entry, desiredAmt);
            if (actualAmt < desiredAmt) {
                cmd.getPlayer().sendMessage(
                        String.format("You did not have enough to sell " + ChatColor.RED + "%d %s" + ChatColor.RESET + ", so only %s were sold.",
                                desiredAmt,
                                desiredAmt == 1 ? "item" : "items",
                                Format.number(actualAmt)
                        )
                );
            }
        }
        catch (NumberFormatException e) {
            if (cmd.getArg(1).equalsIgnoreCase("all")) {
                actualAmt = Main.clearItems(cmd.getPlayer(), entry);
            }
            else if (cmd.getArg(1).equalsIgnoreCase("most")) {
                actualAmt = Main.clearItems(cmd.getPlayer(), entry) - 1;
                ItemStack inHand = entry.toItemStack();
                inHand.setAmount(1);
                cmd.getPlayer().setItemInHand(inHand);
                itemsToSell.setAmount(actualAmt);
                sell(cmd, itemsToSell,true);
                return true;
            }
            else {
                Main.sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "amount"));
                return true;
            }
        }
        itemsToSell.setAmount(actualAmt);
        sell(cmd, itemsToSell, true);
        return true;
    }

    private static double sell(ShopCmd cmd, ItemStack itemsToSell, boolean showExtra)
    {
        BaxShop shop = cmd.getShop();
        BaxEntry entry = shop.findEntry(itemsToSell);
        if (entry == null || entry.refundPrice < 0) {
            Main.sendError(cmd.getPlayer(), Resources.NOT_FOUND_SHOPITEM);
            return -1.0;
        }

        String name = ItemNames.getName(itemsToSell);

        BaxEntry req = new BaxEntry();
        req.setItem(itemsToSell);

        req.refundPrice = entry.refundPrice;

        SellRequest request = new SellRequest(shop.id, shop.owner, cmd.getPlayer().getName(), req);

        double price = Main.roundTwoPlaces((double)itemsToSell.getAmount() * entry.refundPrice);

        if (shop.sellRequests) {
            Main.getState().sendNotification(shop.owner, request);
            cmd.getPlayer().sendMessage(
                    String.format("Your request to sell %s for %s has been sent.",
                            Format.itemname(itemsToSell.getAmount(), name),
                            Format.money(price)
                    )
            );
            if (showExtra) {
                cmd.getPlayer().sendMessage(String.format("This request will expire in %s days.", Format.number(Resources.EXPIRE_TIME_DAYS)));
            }
        }
        else {
            int error = request.autoAccept(cmd.getPlayer());
            if (error == 1) {
                cmd.getPlayer().sendMessage(String.format(
                        "You have sold %s for %s to %s.",
                        Format.itemname(itemsToSell.getAmount(), name),
                        Format.money(price),
                        Format.username(shop.owner)
                        )
                );
                if (showExtra) {
                    cmd.getPlayer().sendMessage(String.format(Resources.CURRENT_BALANCE, Format.money2(Main.getEconomy().getBalance(cmd.getPlayer().getName()))));
                }
                return price;
            }
            else if (error == 0) {
                Main.getState().sendNotification(shop.owner, request);
                Main.sendError(cmd.getPlayer(),
                        String.format("The owner could not purchase %d %s. A request has been sent to the owner to accept your offer at a later time.",
                                itemsToSell.getAmount(), name)
                );
                if (showExtra) {
                    Main.sendError(cmd.getPlayer(), String.format("This request will expire in %d days.", Resources.EXPIRE_TIME_DAYS));
                }
            }
        }
        return -1.0;
    }
}
