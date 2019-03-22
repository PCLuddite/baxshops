/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.StoredData;

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
        assert actor.getShop() != null;
        if (actor.getShop().getLocations().size() == 1) {
            if (actor.getShop().size() > 0 && !actor.getShop().hasFlagInfinite()) {
                actor.sendError("There is still inventory at this shop!");
                actor.sendError("Please remove all inventory before deleting it.");
            }
            else {
                ShopPlugin.removeShop(actor.getPlayer(), actor.getShop());
                ShopPlugin.clearSelection(actor.getPlayer());
            }
        }
        else {
            ShopPlugin.removeLocation(actor.getPlayer(), actor.getSelection().getLocation());
            ShopPlugin.clearSelection(actor.getPlayer());
        }
    }
    
}
