/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.states.State_30;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class BuyClaim extends StandardNote implements Claimable
{
    public BuyClaim(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public BuyClaim(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public BuyClaim(Map<String, Object> args)
    {
        super(args);
    }

    @Override
    public void deserialize30(@NotNull SafeMap map)
    {
        buyer = State_30.getPlayerId(map.getString("buyer"));
        seller = State_30.getPlayerId(map.getString("seller"));
        shopId = BaxShop.DUMMY_UUID;
        entry = map.getBaxEntry("entry");
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getBuyer().equals(sender)) {
            return String.format("%s accepted your request to buy %s for %s.",
                Format.username(seller),
                entry.getFormattedName(),
                entry.getFormattedBuyPrice()
            );
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s accepted %s's request to buy %s for %s.",
            Format.username(seller),
            Format.username2(buyer),
            entry.getFormattedName(),
            entry.getFormattedBuyPrice()
        );
    }

    public static BuyClaim deserialize(Map<String, Object> args)
    {
        return new BuyClaim(args);
    }

    public static BuyClaim valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
