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
import tbax.baxshops.Format;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class SaleClaim extends StandardNote implements Claimable
{
    public SaleClaim(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public SaleClaim(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public SaleClaim(Map<String, Object> args)
    {
        super(args);
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getBuyer().equals(sender)) {
            return String.format("You bought %s from %s for %s",
                entry.getFormattedName(), Format.username(buyer), entry.getFormattedSellPrice()
            );
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s has sold %s to %s for %s",
            Format.username(seller), entry.getFormattedName(), Format.username2(buyer), entry.getFormattedSellPrice()
        );
    }

    public static SaleClaim deserialize(Map<String, Object> args)
    {
        return new SaleClaim(args);
    }

    public static SaleClaim valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
