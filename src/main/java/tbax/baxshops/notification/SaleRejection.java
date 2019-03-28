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
public final class SaleRejection extends StandardNote implements Claimable
{
    public SaleRejection(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public SaleRejection(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }
    public SaleRejection(Map<String, Object> args)
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
        if (getSeller().equals(sender)) {
            return String.format("%s rejected your request to sell %s",
                Format.username(buyer), entry.getFormattedName()
            );
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s rejected %s's request to sell %s",
            Format.username(buyer), Format.username2(seller), entry.getFormattedName()
        );
    }

    public static SaleRejection deserialize(Map<String, Object> args)
    {
        return new SaleRejection(args);
    }

    public static SaleRejection valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
