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
import tbax.baxshops.MathUtil;
import tbax.baxshops.serialization.StoredData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuyNotification implements Notification
{
    private BaxEntry entry;
    private UUID buyer;
    private UUID seller;
    private UUID shopId;

    public BuyNotification(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer.getUniqueId();
        this.seller = seller.getUniqueId();
        this.entry = entry.clone();
    }

    public BuyNotification(Map<String, Object> args)
    {
        entry = (BaxEntry)args.get("entry");
        buyer = UUID.fromString((String)args.get("buyer"));
        seller = UUID.fromString((String)args.get("seller"));
        shopId = UUID.fromString((String)args.get("shopId"));
    }

    public OfflinePlayer getBuyer()
    {
        return StoredData.getOfflinePlayer(buyer);
    }

    public BaxEntry getEntry()
    {
        return entry;
    }

    public OfflinePlayer getSeller()
    {
        return StoredData.getOfflinePlayer(seller);
    }

    public UUID getShopId()
    {
        return shopId;
    }

    @Override
    public String getMessage(CommandSender sender)
    {
        if (getSeller().equals(sender)) {
            return String.format("%s bought %s from you for %s.",
                Format.username(buyer),
                entry.getFormattedName(),
                Format.money(MathUtil.multiply(entry.getRetailPrice(), entry.getAmount()))
            );
        }
        else {
            return String.format("%s bought %s from %s for %s.",
                Format.username(buyer),
                entry.getFormattedName(),
                Format.username2(seller),
                Format.money(MathUtil.multiply(entry.getRetailPrice(), entry.getAmount()))
            );
        }
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("entry", entry);
        args.put("buyer", buyer.toString());
        args.put("seller", seller.toString());
        args.put("shopId", shopId.toString());
        return null;
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
