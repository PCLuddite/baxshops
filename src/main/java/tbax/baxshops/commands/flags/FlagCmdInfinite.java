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

public final class FlagCmdInfinite extends FlagCmd
{
    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"infinite", "isinfinite", "inf"};
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
        boolean value = actor.getArgBoolean(2, "Usage:\n/shop flag infinite [true|false]");
        assert shop != null;
        shop.setFlagInfinite(value);
        actor.sendMessage(Format.flag("Infinite items") + " for this shop are " + Format.keyword(value ? "enabled" : "disabled"));
    }
}
