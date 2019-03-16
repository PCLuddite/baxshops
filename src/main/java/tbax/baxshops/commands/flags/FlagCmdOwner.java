/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands.flags;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.errors.PrematureAbortException;

public final class FlagCmdOwner extends FlagCmd
{
    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"owner"};
    }

    @Override
    public boolean requiresRealOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        assert shop != null;
        shop.setOwner(actor.getArgPlayer(2));
        actor.sendMessage(Format.username(shop.getOwner().getName()) + " is now the owner!");
        if (actor.isOwner()) {
            actor.sendMessage("You will still be able to edit this shop until you leave or reselect it.");
        }
    }
}
