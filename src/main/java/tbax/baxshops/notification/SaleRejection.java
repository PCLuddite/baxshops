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
import tbax.baxshops.serialization.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SaleRejection implements Claimable
{
    private OfflinePlayer seller;
    private OfflinePlayer buyer;
    private BaxEntry entry;
    private UUID shopId;

    public SaleRejection(Map<String, Object> args)
    {
        shopId = UUID.fromString((String)args.get("shopId"));
        seller = (OfflinePlayer)args.get("seller");
        buyer = (OfflinePlayer)args.get("buyer");
        entry = (BaxEntry)args.get("entry");
    }

    public SaleRejection(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.seller = seller;
        this.buyer = buyer;
        this.entry = entry;
        this.shopId = shopId;
    }

    public UUID getShopId()
    {
        return shopId;
    }

    public BaxShop getShop()
    {
        return SavedData.getShop(shopId);
    }

    public OfflinePlayer getBuyer()
    {
        return buyer;
    }

    public OfflinePlayer getSeller()
    {
        return seller;
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
        if (sender == null || !seller.equals(sender)) {
            return String.format("%s rejected %s's request to sell %s",
                Format.username(buyer.getName()), Format.username2(seller.getName()), entry.getFormattedName()
                );
        }
        else {
            return String.format("%s rejected your request to sell %s",
                Format.username(buyer.getName()), entry.getFormattedName()
            );
        }
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("shopId", shopId.toString());
        args.put("buyer", buyer);
        args.put("seller", seller);
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
