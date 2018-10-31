package tbax.baxshops.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.Format;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.executer.CmdRequisite;
import tbax.baxshops.help.CommandHelp;
import tbax.baxshops.help.Help;
import tbax.baxshops.serialization.ItemNames;

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
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop restock", "r", null, "restock this shop with your held item");
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 1 || actor.getNumArgs() == 2;
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
        if (actor.getShop().infinite) {
            actor.exitError("This shop does not need to be restocked.");
        }

        ItemStack stack = actor.getItemInHand();
        if (actor.getNumArgs() == 1) {
            actor.appendArg(String.valueOf(actor.getItemInHand().getAmount())); // restock all in hand if nothing specified
        }

        BaxEntry entry = actor.getShop().findEntry(stack);
        if (entry == null) {
            actor.exitError(Resources.NOT_FOUND_SHOPITEM);
        }

        int amt;

        try {
            amt = Integer.parseInt(cmd.getArg(1));
        }
        catch(NumberFormatException ex) {
            if (cmd.getArg(1).equalsIgnoreCase("all")) {
                stack.setAmount(Main.clearItems(cmd.getPlayer(), entry));
                entry.add(stack.getAmount());
            }
            else if (cmd.getArg(1).equalsIgnoreCase("most")) {
                stack.setAmount(Main.clearItems(cmd.getPlayer(), entry) - 1);
                entry.add(stack.getAmount());
                ItemStack inHand = stack.clone();
                inHand.setAmount(1);
                cmd.getPlayer().setItemInHand(inHand);
            }
            else {
                Main.sendError(cmd.getPlayer(), String.format(Resources.INVALID_DECIMAL, "restock amount"));
                Main.sendError(cmd.getPlayer(), Help.RESTOCK.toUsageString());
                return true;
            }
            cmd.getMain().sendInfo(cmd.getPlayer(), String.format("Restocked with %s. The shop now has %s.",
                    Format.itemname(stack.getAmount(), ItemNames.getName(entry)),
                    Format.number(entry.getAmount())
            ));
            return true;
        }

        if (cmd.getPlayer().getItemInHand() != null && amt < cmd.getPlayer().getItemInHand().getAmount()) {
            stack.setAmount(amt);
            cmd.getPlayer().getItemInHand().setAmount(cmd.getPlayer().getItemInHand().getAmount() - amt); // Don't be hoggin all of it!
        }
        else {
            stack.setAmount(Main.clearItems(cmd.getPlayer(), entry, amt)); // Ok, take it all
        }

        if (stack.getAmount() < amt) {
            entry.add(stack.getAmount());
            cmd.getPlayer().setItemInHand(null);
            cmd.getMain().sendInfo(cmd.getPlayer(), String.format("Could only restock with " + ChatColor.RED + "%d %s" + ChatColor.RESET + ". The shop now has %s.",
                    stack.getAmount(), ItemNames.getName(entry),
                    Format.number(entry.getAmount()))
            );
            return true;
        }

        entry.add(stack.getAmount());

        cmd.getMain().sendInfo(cmd.getPlayer(),
                String.format("Restocked with %s in hand. The shop now has %s.",
                        Format.itemname(stack.getAmount(), ItemNames.getName(entry)),
                        Format.number(entry.getAmount())
                )
        );

        if (amt >= cmd.getPlayer().getItemInHand().getAmount()) {
            cmd.getPlayer().setItemInHand(null);
        }
        return true;
    }
}
