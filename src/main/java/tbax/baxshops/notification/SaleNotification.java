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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SaleNotification implements Notification
{
    private UUID shopId;
    private OfflinePlayer buyer;
    private OfflinePlayer seller;
    private BaxEntry entry;

    public SaleNotification(Map<String, Object> args)
    {
        shopId = UUID.fromString((String)args.get("shopId"));
        seller = (OfflinePlayer) args.get("seller");
        entry = (BaxEntry)args.get("entry");
        buyer = (OfflinePlayer)args.get("buyer");
    }

    public SaleNotification(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer;
        this.seller = seller;
        this.entry = entry;
    }

    public UUID getShopId()
    {
        return shopId;
    }

    public OfflinePlayer getBuyer()
    {
        return buyer;
    }

    public OfflinePlayer getSeller()
    {
        return seller;
    }

    public BaxEntry getItem()
    {
        return entry;
    }

    @Override
    public String getMessage(CommandSender sender)
    {
        if (sender == null || !seller.equals(sender)) {
            return String.format("%s accepted %s's request to sell %s for %s.",
                Format.username(buyer.getName()), Format.username2(seller.getName()), entry.getFormattedName(), entry.getFormattedSellPrice()
            );
        }
        else {
            return String.format("%s accepted your request to sell %s for %s.",
                Format.username(buyer.getName()), entry.getFormattedName(), entry.getFormattedSellPrice()
            );
        }
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("shopId", shopId.toString());
        args.put("seller", seller);
        args.put("buyer", buyer);
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
