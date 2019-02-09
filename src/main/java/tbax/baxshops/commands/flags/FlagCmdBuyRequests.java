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

public final class FlagCmdBuyRequests extends FlagCmd
{
    @Override
    public String[] getAliases()
    {
        return new String[]{"buyrequests", "buyrequest", "buy_request", "buy_requests"};
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        boolean value = actor.getArgBoolean(2, "Usage:\n/shop flag buyrequests [true|false]");
        assert shop != null;
        shop.setFlagBuyRequests(value);
        actor.sendMessage(Format.flag("Buy requests") + " for this shop are " + Format.keyword(value ? "enabled" : "disabled"));
    }
}
