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
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.StoredData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class SaleClaim extends Claimable
{
    private UUID buyer;
    private UUID seller;
    private BaxEntry entry;
    private UUID shopId;

    public SaleClaim(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer.getUniqueId();
        this.seller = seller.getUniqueId();
        this.entry = new BaxEntry(entry);
    }

    public SaleClaim(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        shopId = map.getUUID("shopId");
        buyer = map.getUUID("buyer");
        seller = map.getUUID("seller");
        entry = map.getBaxEntry("entry");
    }

    public OfflinePlayer getSeller()
    {
        return StoredData.getOfflinePlayer(seller);
    }

    public OfflinePlayer getBuyer()
    {
        return StoredData.getOfflinePlayer(buyer);
    }

    @Override
    public BaxEntry getEntry()
    {
        return entry;
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

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("shopId", shopId.toString());
        args.put("buyer", getBuyer().getUniqueId().toString());
        args.put("seller", getSeller().getUniqueId().toString());
        args.put("entry", entry);
        return args;
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
