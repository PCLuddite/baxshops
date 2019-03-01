/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.commands.flags;

import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.Resources;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.StoredData;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.List;

public final class FlagCmdOwner extends FlagCmd
{
    @Override
    public String[] getAliases()
    {
        return new String[]{"owner"};
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        assert shop != null;

        if (actor.isArgUuid(2)) {
            StoredPlayer player = StoredData.getOfflinePlayerSafe(actor.getArgUuid(2));
            shop.setOwner(player);
        }
        else {
            List<StoredPlayer> players = StoredData.getOfflinePlayer(actor.getArg(2));
            if (players.size() == 1) {
                shop.setOwner(players.get(0));
            }
            else {
                actor.exitError(Resources.TooManyPlayers(players));
            }
        }
        actor.sendMessage(Format.username(shop.getOwner().getName()) + " is now the owner!");
        if (actor.isOwner()) {
            actor.sendMessage("You will still be able to edit this shop until you leave or reselect it.");
        }
    }
}
