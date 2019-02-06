/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.commands.flags;

import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.errors.PrematureAbortException;

public final class FlagCmdSellToShop extends FlagCmd
{
    @Override
    public String[] getAliases()
    {
        return new String[]{"selltoshop", "sell_to_shop"};
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        boolean value = actor.getArgBoolean(2, "Usage:\n/shop flag sell_to_shop [true|false]");
        shop.setFlagSellToShop(value);
        actor.sendMessage(Format.flag("Sell to Shop") + " is " + Format.keyword(value ? "enabled" : "disabled"));
    }
}
