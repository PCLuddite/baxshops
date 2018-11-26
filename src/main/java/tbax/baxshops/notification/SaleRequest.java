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
import tbax.baxshops.serialization.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SaleRequest implements Request
{
    private OfflinePlayer buyer;
    private OfflinePlayer seller;
    private BaxEntry entry;
    private UUID shopId;

    public SaleRequest(Map<String, Object> args)
    {
        shopId = UUID.fromString((String)args.get("shopId"));
        buyer = (OfflinePlayer)args.get("buyer");
        seller = (OfflinePlayer)args.get("seller");
        entry = (BaxEntry)args.get("entry");
    }

    public SaleRequest(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
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

    public BaxEntry getEntry()
    {
        return entry;
    }

    @Override
    public boolean accept(ShopCmdActor acceptingActor)
    {
        try {
            PlayerUtil.sellItem(shopId, buyer, seller, entry);
            SavedData.sendNotification(seller, new SaleNotification(shopId, buyer, seller, entry.clone()));
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
        SavedData.sendNotification(seller, rejection);
        rejectingActor.sendError("Offer rejected");
        return true;
    }

    @Override
    public String getMessage(CommandSender sender)
    {
        if (sender == null || !sender.equals(buyer)) {
            return String.format("%s wants to sell %s to %s for %s.",
                Format.username(seller.getName()), entry.getFormattedName(), Format.username2(buyer.getName()), entry.getFormattedSellPrice()
            );
        }
        else {
            return String.format("%s wants to sell you %s for %s.",
                Format.username(seller.getName()), entry.getFormattedName(), entry.getFormattedSellPrice()
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

    public static SaleRequest deserialize(Map<String, Object> args)
    {
        return new SaleRequest(args);
    }

    public static SaleRequest valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
