/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import tbax.baxshops.*;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.StoredData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class BuyRequest implements Request
{
    private UUID buyer;
    private UUID seller;
    private UUID shopId;
    private BaxEntry entry;

    public BuyRequest(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer.getUniqueId();
        this.seller = seller.getUniqueId();
        this.entry = new BaxEntry(entry);
    }

    public BuyRequest(Map<String, Object> args)
    {
        shopId = UUID.fromString((String)args.get("shopId"));
        buyer = UUID.fromString((String)args.get("buyer"));
        seller = UUID.fromString((String)args.get("seller"));
        entry = (BaxEntry)args.get("entry");
    }

    public OfflinePlayer getBuyer()
    {
        return StoredData.getOfflinePlayer(buyer);
    }

    public OfflinePlayer getSeller()
    {
        return StoredData.getOfflinePlayer(seller);
    }

    public UUID getShopId()
    {
        return shopId;
    }

    public BaxEntry getEntry()
    {
        return entry;
    }

    @Override
    public boolean accept(ShopCmdActor acceptingActor)
    {
        try {
            double price = MathUtil.multiply(entry.getAmount(), entry.getRetailPrice());

            Economy econ = ShopPlugin.getEconomy();

            if (!econ.has(getBuyer(), price)) {
                acceptingActor.exitError(Resources.NO_MONEY_SELLER);
            }

            econ.withdrawPlayer(getBuyer(), price);
            econ.depositPlayer(getSeller(), price);

            BuyClaim n = new BuyClaim(shopId, buyer, seller, entry);
            ShopPlugin.sendNotification(getBuyer(), n);

            acceptingActor.sendMessage("Offer accepted");
            acceptingActor.sendMessage(String.format(Resources.CURRENT_BALANCE, Format.money2(ShopPlugin.getEconomy().getBalance(acceptingActor.getPlayer()))));
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
        BaxShop shop = StoredData.getShop(shopId);
        if (shop == null) {
            DeletedShopClaim shopDeleted = new DeletedShopClaim(buyer, entry);
            ShopPlugin.sendNotification(getBuyer(), shopDeleted);
        }
        else if (!shop.hasFlagInfinite()) {
            BaxEntry shopEntry = shop.find(entry.getItemStack());
            if (shopEntry == null) {
                shop.add(entry);
            }
            else {
                shopEntry.add(entry.getAmount());
            }
        }

        BuyRejection n = new BuyRejection(shopId, buyer, seller, entry);
        ShopPlugin.sendNotification(buyer, n);
        rejectingActor.sendError("Offer rejected");
        return true;
    }

    @Override
    public String getMessage(CommandSender sender)
    {
        if (getSeller().equals(sender)) {
            return String.format("%s wants to buy %s from you for %s.",
                Format.username(buyer), entry.getFormattedName(), entry.getFormattedBuyPrice()
            );
        }
        else {
            return String.format("%s wants to buy %s from %s for %s.",
                Format.username(buyer), entry.getFormattedName(), Format.username2(seller), entry.getFormattedBuyPrice()
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

    public static BuyRequest deserialize(Map<String, Object> args)
    {
        return new BuyRequest(args);
    }

    public static BuyRequest valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
