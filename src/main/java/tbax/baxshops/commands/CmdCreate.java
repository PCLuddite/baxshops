/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.BaxShop;
import tbax.baxshops.CommandHelpArgument;
import tbax.baxshops.Format;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.serialization.SavedData;

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
    public CommandHelp getHelp(ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("create a new shop");
        if (actor.isAdmin()) {
            help.setArgs(
                new CommandHelpArgument("owner", "the owner of the shop", true),
                new CommandHelpArgument("infinite", "whether the shop is infinite", false, "no")
            );
        }
        return help;
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
        OfflinePlayer owner;
        if (actor.isAdmin()) {
            owner = actor.getPlugin().getServer().getOfflinePlayer(actor.getArg(1));
        }
        else {
            owner = actor.getPlayer();
        }

        Location loc = actor.getPlayer().getLocation().getWorld().getBlockAt(actor.getPlayer().getLocation()).getLocation();

        if (!actor.isAdmin() && !actor.getInventory().containsAtLeast(new ItemStack(Material.SIGN), 1)) {
            actor.exitError("You need a sign to set up a shop.");
        }

        BaxShop shop = new BaxShop();
        shop.setOwner(owner);
        
        if (!SavedData.addShop(actor.getPlayer(), shop))
            return;;

        Block b = shop.buildShopSign(
            loc, new String[] {
                "",
                (owner.getName().length() < 13 ? owner : owner.getName().substring(0, 12) + '…') + "'s",
                "shop",
                ""
            }
        );

        if (actor.isAdmin() && actor.getNumArgs() == 3) {
            shop.setFlagInfinite(actor.getArgBoolean(2));
        }

        shop.setFlagSellRequests(shop.hasFlagInfinite());
        shop.setFlagBuyRequests(false);

        if (!actor.isAdmin()) {
            actor.getInventory().remove(Material.SIGN);
        }
        actor.sendMessage(Format.username(shop.getOwner().getName()) + "'s shop has been created.");
        actor.sendMessage(Format.flag("Buy requests") + " for this shop are " + Format.keyword(shop.hasFlagBuyRequests() ? "on" : "off"));
        actor.sendMessage(Format.flag("Sell requests") + " for this shop are " + Format.keyword(shop.hasFlagSellRequests() ? "on" : "off"));
    }
}
