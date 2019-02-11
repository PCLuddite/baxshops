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

public class FlagCmdNotify extends FlagCmd
{
    @Override
    public String[] getAliases()
    {
        return new String[]{"notify"};
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 3;
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        boolean value = actor.getArgBoolean(2, "Usage:\n/shop flag notify [true|false]");
        assert shop != null;
        shop.setFlagNotify(value);
        actor.sendMessage(Format.flag("Notifications") + " for this shop are " + Format.keyword(value ? "enabled" : "disabled"));
    }
}
