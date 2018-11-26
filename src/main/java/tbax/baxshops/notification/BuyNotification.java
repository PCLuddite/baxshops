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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuyNotification implements Notification
{
    private BaxEntry entry;
    private OfflinePlayer buyer;
    private OfflinePlayer seller;
    private UUID shopId;

    public BuyNotification(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer;
        this.seller = seller;
        this.entry = entry.clone();
    }

    public BuyNotification(Map<String, Object> args)
    {
        entry = (BaxEntry)args.get("entry");
        buyer = (OfflinePlayer)args.get("buyer");
        seller = (OfflinePlayer)args.get("seller");
        shopId = UUID.fromString((String)args.get("shopId"));
    }

    public OfflinePlayer getBuyer()
    {
        return buyer;
    }

    public BaxEntry getEntry()
    {
        return entry;
    }

    public OfflinePlayer getSeller()
    {
        return seller;
    }

    public UUID getShopId()
    {
        return shopId;
    }

    @Override
    public String getMessage(CommandSender sender)
    {
        if (sender == null || !sender.equals(seller)) {
            return String.format("%s bought %s from %s for %s.",
                Format.username(buyer.getName()),
                entry.getFormattedName(),
                Format.username2(seller.getName()),
                Format.money(MathUtil.multiply(entry.getRetailPrice(), entry.getAmount()))
            );
        }
        else {
            return String.format("%s bought %s from you for %s.",
                Format.username(buyer.getName()),
                entry.getFormattedName(),
                Format.money(MathUtil.multiply(entry.getRetailPrice(), entry.getAmount()))
            );
        }
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String,Object> args = new HashMap<>();
        args.put("entry", entry);
        args.put("buyer", buyer);
        args.put("seller", seller);
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
