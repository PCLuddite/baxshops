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
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.StoredData;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.List;
import java.util.UUID;

public final class CmdCreate extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "create";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"create","mk"};
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
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
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        if (actor.isAdmin())
            return actor.getNumArgs() == 2 || actor.getNumArgs() == 3;
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return false;
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
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        OfflinePlayer owner;
        if (actor.isAdmin()) {
            owner = actor.getArgPlayer(1);
        }
        else {
            owner = actor.getPlayer();
        }

        Location loc = actor.getPlayer().getLocation();
        loc = loc.getWorld().getBlockAt(loc).getLocation();
        assert actor.getInventory() != null;
        if (!actor.isAdmin() && !actor.getInventory().containsAtLeast(new ItemStack(Material.SIGN), 1)) {
            actor.exitError("You need a sign to set up a shop.");
        }

        BaxShop shop = new BaxShop();
        shop.setOwner(owner);

        shop.buildShopSign(loc,
            "",
            (owner.getName().length() < 13 ? owner.getName() : owner.getName().substring(0, 12) + '…') + "'s",
            "shop",
            ""
        );

        if (!ShopPlugin.addShop(actor.getPlayer(), shop))
            return;

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
