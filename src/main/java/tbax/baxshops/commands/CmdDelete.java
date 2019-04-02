/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.Resources;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.errors.PrematureAbortException;

public final class CmdDelete extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "delete";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"delete","del"};
    }
    
    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return true;
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
        BaxShop shop = actor.getShop();
        assert shop != null;
        if (shop.getLocations().size() == 1) {
            if (!shop.isEmpty() && !actor.getShop().hasFlagInfinite()) {
                actor.sendError("There is still inventory at this shop!");
                actor.sendError("Please remove all inventory before deleting it.");
            }
            else {
                removeShop(actor, actor.getShop());
                actor.sendMessage("%s's shop has been deleted", Format.username(shop.getOwner().getName()));
            }
        }
        else {
            ShopPlugin.removeLocation(shop.getId(), actor.getSelection().getLocation());
            changeSignText(actor, actor.getSelection().getLocation());
            actor.sendMessage("This shop location has been closed");
        }
        ShopPlugin.clearSelection(actor.getPlayer());
    }

    private void removeShop(ShopCmdActor actor, BaxShop shop) throws PrematureAbortException
    {
        for(Location loc : shop.getLocations()) {
            changeSignText(actor, loc);
            ShopPlugin.removeLocation(shop.getId(), loc);
        }
        ShopPlugin.removeShop(shop.getId());
    }

    private void changeSignText(ShopCmdActor actor, Location loc) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        assert shop != null;
        try {
            Block b = loc.getBlock();
            Sign sign = (Sign) b.getState();
            sign.setLine(0, Resources.SIGN_CLOSED[0]);
            sign.setLine(1, Resources.SIGN_CLOSED[1]);
            sign.setLine(2, (shop.getOwner().equals(actor.getPlayer()) ? "the owner" : "an admin") + ".");
            sign.setLine(3, "");
            sign.update();
        }
        catch (NullPointerException | ClassCastException e) {
            actor.sendError("Unable to change the sign text at " + Format.location(loc));
        }
    }
}
