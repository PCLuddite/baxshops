/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.Format;
import tbax.baxshops.serialization.StoredData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SaleNotification implements Notification
{
    private UUID shopId;
    private UUID buyer;
    private UUID seller;
    private BaxEntry entry;

    public SaleNotification(Map<String, Object> args)
    {
        shopId = UUID.fromString((String)args.get("shopId"));
        seller = UUID.fromString((String) args.get("seller"));
        buyer = UUID.fromString((String)args.get("buyer"));
        entry = (BaxEntry)args.get("entry");
    }

    public SaleNotification(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer.getUniqueId();
        this.seller = seller.getUniqueId();
        this.entry = entry;
    }

    public SaleNotification(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        this(shopId, StoredData.getOfflinePlayer(buyer), StoredData.getOfflinePlayer(seller), entry);
    }

    public UUID getShopId()
    {
        return shopId;
    }

    public OfflinePlayer getBuyer()
    {
        return StoredData.getOfflinePlayer(buyer);
    }

    public OfflinePlayer getSeller()
    {
        return StoredData.getOfflinePlayer(seller);
    }

    public BaxEntry getItem()
    {
        return entry;
    }

    @Override
    public String getMessage(CommandSender sender)
    {
        if (getSeller().equals(sender)) {
            return String.format("%s accepted your request to sell %s for %s.",
                Format.username(buyer), entry.getFormattedName(), entry.getFormattedSellPrice()
            );
        }
        else {
            return String.format("%s accepted %s's request to sell %s for %s.",
                Format.username(buyer), Format.username2(seller), entry.getFormattedName(), entry.getFormattedSellPrice()
            );
        }
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("shopId", shopId.toString());
        args.put("seller", seller.toString());
        args.put("buyer", buyer.toString());
        args.put("entry", entry);
        return args;
    }

    public static SaleNotification deserialize(Map<String, Object> args)
    {
        return new SaleNotification(args);
    }

    public static SaleNotification valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
