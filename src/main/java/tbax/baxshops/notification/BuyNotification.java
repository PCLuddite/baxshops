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
import tbax.baxshops.MathUtil;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.states.State_30;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class BuyNotification extends StandardNote
{
    public BuyNotification(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public BuyNotification(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public BuyNotification(Map<String, Object> args)
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
            return String.format("%s bought %s from you for %s.",
                Format.username(buyer),
                entry.getFormattedName(),
                Format.money(MathUtil.multiply(entry.getRetailPrice(), entry.getAmount()))
            );
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s bought %s from %s for %s.",
            Format.username(buyer),
            entry.getFormattedName(),
            Format.username2(seller),
            Format.money(MathUtil.multiply(entry.getRetailPrice(), entry.getAmount()))
        );
    }

    public static BuyNotification deserialize(Map<String, Object> args)
    {
        return new BuyNotification(args);
    }

    public static BuyNotification valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
