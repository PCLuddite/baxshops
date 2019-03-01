/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import tbax.baxshops.*;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.StoredData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class SaleRequest implements Request
{
    private UUID buyer;
    private UUID seller;
    private BaxEntry entry;
    private UUID shopId;

    public SaleRequest(Map<String, Object> args)
    {
        shopId = UUID.fromString((String)args.get("shopId"));
        buyer = UUID.fromString((String)args.get("buyer"));
        seller = UUID.fromString((String)args.get("seller"));
        entry = (BaxEntry)args.get("entry");
    }

    public SaleRequest(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer.getUniqueId();
        this.seller = seller.getUniqueId();
        this.entry = entry;
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

    public BaxEntry getEntry()
    {
        return entry;
    }

    @Override
    public boolean accept(ShopCmdActor acceptingActor)
    {
        try {
            PlayerUtil.sellItem(shopId, buyer, seller, entry);
            StoredData.sendNotification(seller, new SaleNotification(shopId, buyer, seller, entry));
            return true;
        }
        catch (PrematureAbortException e) {
            acceptingActor.sendMessage(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean reject(ShopCmdActor rejectingActor)
    {
        SaleRejection rejection = new SaleRejection(shopId, buyer, seller, entry);
        StoredData.sendNotification(seller, rejection);
        rejectingActor.sendError("Offer rejected");
        return true;
    }

    @Override
    public String getMessage(CommandSender sender)
    {
        if (getBuyer().equals(sender)) {
            return String.format("%s wants to sell you %s for %s.",
                Format.username(seller), entry.getFormattedName(), entry.getFormattedSellPrice()
            );
        }
        else {
            return String.format("%s wants to sell %s to %s for %s.",
                Format.username(seller), entry.getFormattedName(), Format.username2(buyer), entry.getFormattedSellPrice()
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

    public static SaleRequest deserialize(Map<String, Object> args)
    {
        return new SaleRequest(args);
    }

    public static SaleRequest valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
