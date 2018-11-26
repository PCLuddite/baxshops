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
import tbax.baxshops.serialization.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuyRequest implements Request
{
    private OfflinePlayer buyer;
    private OfflinePlayer seller;
    private UUID shopId;
    private BaxEntry entry;

    public BuyRequest(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer;
        this.seller = seller;
        this.entry = entry.clone();
    }

    public BuyRequest(Map<String, Object> args)
    {
        shopId = UUID.fromString((String)args.get("shopId"));
        buyer = (OfflinePlayer)args.get("buyer");
        seller = (OfflinePlayer)args.get("seller");
        entry = (BaxEntry)args.get("entry");
    }

    public OfflinePlayer getBuyer()
    {
        return buyer;
    }

    public OfflinePlayer getSeller()
    {
        return seller;
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

            Economy econ = Main.getEconomy();

            if (!econ.has(buyer, price)) {
                acceptingActor.exitError(Resources.NO_MONEY_SELLER);
            }

            econ.withdrawPlayer(buyer, price);
            econ.depositPlayer(seller, price);

            BuyClaim n = new BuyClaim(shopId, buyer, seller, entry);
            SavedData.sendNotification(buyer, n);

            acceptingActor.sendMessage("Offer accepted");
            acceptingActor.sendMessage(String.format(Resources.CURRENT_BALANCE, Format.money2(Main.getEconomy().getBalance(acceptingActor.getPlayer()))));
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
        BaxShop shop = SavedData.getShop(shopId);
        if (shop == null) {
            DeletedShopClaim shopDeleted = new DeletedShopClaim(buyer, entry);
            SavedData.sendNotification(buyer, shopDeleted);
        }
        else if (!shop.hasFlagInfinite()) {
            BaxEntry shopEntry = shop.findEntry(entry.getItemStack());
            if (shopEntry == null) {
                shop.addEntry(entry);
            }
            else {
                shopEntry.add(entry.getAmount());
            }
        }

        BuyRejection n = new BuyRejection(shopId, seller, buyer, entry);
        SavedData.sendNotification(buyer, n);
        rejectingActor.sendMessage("Offer rejected");
        return true;
    }

    @Override
    public String getMessage(CommandSender sender)
    {
        if (sender == null || !seller.equals(sender)) {
            return String.format("%s wants to buy %s from %s for %s.",
                Format.username(buyer.getName()), entry.getFormattedName(), Format.username2(seller.getName()), entry.getFormattedBuyPrice()
            );
        }
        else {
            return String.format("%s wants to buy %s from you for %s.",
                Format.username(buyer.getName()), entry.getFormattedName(), entry.getFormattedBuyPrice()
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

    public static BuyRequest deserialize(Map<String, Object> args)
    {
        return new BuyRequest(args);
    }

    public static BuyRequest valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
