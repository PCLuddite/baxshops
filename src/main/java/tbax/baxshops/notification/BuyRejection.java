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
import tbax.baxshops.BaxEntry;
import tbax.baxshops.Format;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.StoredData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class BuyRejection implements Notification
{
    private UUID seller;
    private UUID buyer;
    private BaxEntry entry;
    private UUID shopId;

    public BuyRejection(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer.getUniqueId();
        this.seller = seller.getUniqueId();
        this.entry = new BaxEntry(entry);
    }

    public BuyRejection(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        shopId = map.getUUID("shopId");
        buyer = map.getUUID("buyer");
        seller = map.getUUID("seller");
        entry = map.getBaxEntry("entry");
    }

    public BuyRejection(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        this(shopId, StoredData.getOfflinePlayer(seller), StoredData.getOfflinePlayer(buyer), entry);
    }

    public OfflinePlayer getBuyer()
    {
        return StoredData.getOfflinePlayer(buyer);
    }

    public OfflinePlayer getSeller()
    {
        return StoredData.getOfflinePlayer(seller);
    }

    public BaxEntry getEntry()
    {
        return entry;
    }

    @Override
    public String getMessage(CommandSender sender)
    {
        if (getSeller().equals(sender)) {
            return String.format("%s " + ChatColor.RED + "rejected" + ChatColor.RESET + " your request to buy %s for %s.",
                Format.username(seller),
                entry.getFormattedName(),
                entry.getFormattedBuyPrice()
            );
        }
        else {
            return String.format("%s " + ChatColor.RED + "rejected" + ChatColor.RESET + " %s's request to buy %s for %s.",
                Format.username(seller),
                Format.username2(buyer),
                entry.getFormattedName(),
                entry.getFormattedBuyPrice()
            );
        }
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

    public static BuyRejection deserialize(Map<String, Object> args)
    {
        return new BuyRejection(args);
    }

    public static BuyRejection valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
