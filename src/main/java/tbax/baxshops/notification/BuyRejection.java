/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.notification;

import org.bukkit.ChatColor;
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
public final class BuyRejection extends StandardNote implements Notification
{
    public BuyRejection(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public BuyRejection(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public BuyRejection(Map<String, Object> args)
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
            return String.format("%s " + ChatColor.RED + "rejected" + ChatColor.RESET + " your request to buy %s for %s.",
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
        return String.format("%s " + ChatColor.RED + "rejected" + ChatColor.RESET + " %s's request to buy %s for %s.",
            Format.username(seller),
            Format.username2(buyer),
            entry.getFormattedName(),
            entry.getFormattedBuyPrice()
        );
    }

    public static BuyRejection deserialize(Map<String, Object> args)
    {
        return new BuyRejection(args);
    }

    public static BuyRejection valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
