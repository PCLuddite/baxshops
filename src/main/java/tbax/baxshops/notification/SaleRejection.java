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
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.serialization.StoredData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SaleRejection implements Claimable
{
    private UUID seller;
    private UUID buyer;
    private BaxEntry entry;
    private UUID shopId;

    public SaleRejection(Map<String, Object> args)
    {
        shopId = UUID.fromString((String)args.get("shopId"));
        seller = UUID.fromString((String)args.get("seller"));
        buyer = UUID.fromString((String)args.get("buyer"));
        entry = (BaxEntry)args.get("entry");
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
        this(shopId, StoredData.getOfflinePlayer(buyer), StoredData.getOfflinePlayer(seller), entry);
    }

    public UUID getShopId()
    {
        return shopId;
    }

    public BaxShop getShop()
    {
        return StoredData.getShop(shopId);
    }

    public OfflinePlayer getBuyer()
    {
        return StoredData.getOfflinePlayer(buyer);
    }

    public OfflinePlayer getSeller()
    {
        return StoredData.getOfflinePlayer(seller);
    }

    @Override
    public BaxEntry getEntry()
    {
        return entry;
    }

    @Override
    public boolean claim(ShopCmdActor actor)
    {
        return actor.tryGiveItem(entry.toItemStack());
    }

    @Override
    public String getMessage(CommandSender sender)
    {
        if (getSeller().equals(sender)) {
            return String.format("%s rejected your request to sell %s",
                Format.username(buyer), entry.getFormattedName()
            );
        }
        else {
            return String.format("%s rejected %s's request to sell %s",
                Format.username(buyer), Format.username2(seller), entry.getFormattedName()
            );
        }
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("shopId", shopId.toString());
        args.put("buyer", buyer.toString());
        args.put("seller", seller.toString());
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
