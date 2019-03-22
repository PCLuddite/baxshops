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
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.SafeMap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class SaleRejection implements Claimable
{
    private UUID seller;
    private UUID buyer;
    private BaxEntry entry;
    private UUID shopId;

    public SaleRejection(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        shopId = map.getUUID("shopId");
        seller = map.getUUID("seller");
        buyer = map.getUUID("buyer");
        entry = map.getBaxEntry("entry");
    }

    public SaleRejection(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.seller = seller.getUniqueId();
        this.buyer = buyer.getUniqueId();
        this.entry = entry;
        this.shopId = shopId;
    }

    public SaleRejection(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        this(shopId, ShopPlugin.getOfflinePlayer(buyer), ShopPlugin.getOfflinePlayer(seller), entry);
    }

    public UUID getShopId()
    {
        return shopId;
    }

    public BaxShop getShop()
    {
        return ShopPlugin.getShop(shopId);
    }

    public OfflinePlayer getBuyer()
    {
        return ShopPlugin.getOfflinePlayer(buyer);
    }

    public OfflinePlayer getSeller()
    {
        return ShopPlugin.getOfflinePlayer(seller);
    }

    @Override
    public BaxEntry getEntry()
    {
        return entry;
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getSeller().equals(sender)) {
            return String.format("%s rejected your request to sell %s",
                Format.username(buyer), entry.getFormattedName()
            );
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s rejected %s's request to sell %s",
            Format.username(buyer), Format.username2(seller), entry.getFormattedName()
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

    public static SaleRejection deserialize(Map<String, Object> args)
    {
        return new SaleRejection(args);
    }

    public static SaleRejection valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
