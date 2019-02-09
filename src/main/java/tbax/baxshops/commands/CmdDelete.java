/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.ShopPlugin;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.StoredData;

public final class CmdDelete extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "delete";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"delete","del"};
    }
    
    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 2;
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
        return false;
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        assert actor.getShop() != null;
        if (actor.getShop().getLocations().size() == 1) {
            if (actor.getShop().size() > 0 && !actor.getShop().hasFlagInfinite()) {
                actor.sendError("There is still inventory at this shop!");
                actor.sendError("Please remove all inventory before deleting it.");
            }
            else {
                StoredData.removeShop(actor.getPlayer(), actor.getShop());
                ShopPlugin.clearSelection(actor.getPlayer());
            }
        }
        else {
            StoredData.removeLocation(actor.getPlayer(), actor.getSelection().getLocation());
            ShopPlugin.clearSelection(actor.getPlayer());
        }
    }
    
}
