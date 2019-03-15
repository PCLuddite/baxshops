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
import tbax.baxshops.MathUtil;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.StoredData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class BuyNotification implements Notification
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
        this.entry = new BaxEntry(entry);
    }

    public BuyNotification(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        entry = map.getBaxEntry("entry");
        buyer = map.getUUID("buyer");
        seller = map.getUUID("seller");
        shopId = map.getUUID("shopId");
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

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("entry", entry);
        args.put("buyer", getBuyer().getUniqueId().toString());
        args.put("seller", getSeller().getUniqueId().toString());
        args.put("shopId", shopId.toString());
        return args;
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
