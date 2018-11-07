package tbax.baxshops.commands;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.help.CommandHelp;

public class CmdCreate extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "create";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"create","mk"};
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop create", "mk", "<owner> [inf=no]", "create a new shop",
                CommandHelp.args(
                        "owner", "the owner of the shop",
                        "inf", "whether the shop is infinite"));
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        if (actor.isAdmin())
            return actor.getNumArgs() == 2 || actor.getNumArgs() == 3;
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(ShopCmdActor actor)
    {
        return false;
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
        String owner = actor.isAdmin() ? actor.getArg(1) : actor.getPlayer().getName();
        BaxShop shop = new BaxShop();
        shop.addLocation(actor.getPlayer().getLocation().getWorld().getBlockAt(actor.getPlayer().getLocation()).getLocation());
        shop.setOwner(owner);

        if (buildShopSign(cmd, new String[] {
                "",
                (owner.length() < 13 ? owner : owner.substring(0, 12) + '…') + "'s",
                "shop",
                ""
        }) == null) {
            return true; // Couldn't build the sign. Retreat!
        }

         admin && cmd.getNumArgs() > 2 && (cmd.getArg(2).equalsIgnoreCase("yes") || cmd.getArg(2).equalsIgnoreCase("true"));
        shop.sellRequests = !shop.infinite;
        shop.buyRequests = false;

        if (!Main.getState().addShop(cmd.getPlayer(), shop)) {
            if (!admin) {
                cmd.getPlayer().getInventory().addItem(new ItemStack(Material.SIGN)); // give the sign back
            }
            return true;
        }
        cmd.getPlayer().sendMessage(Format.username(shop.owner) + "'s shop has been created.");
        cmd.getPlayer().sendMessage(Format.flag("Buy requests") + " for this shop are " + Format.keyword(shop.buyRequests ? "on" : "off"));
        cmd.getPlayer().sendMessage(Format.flag("Sell requests") + " for this shop are " + Format.keyword(shop.sellRequests ? "on" : "off"));
        return true;
    }
}
