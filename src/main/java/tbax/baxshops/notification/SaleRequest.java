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
import tbax.baxshops.PlayerUtil;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.errors.PrematureAbortException;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class SaleRequest extends StandardNote implements Request
{
    public SaleRequest(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public SaleRequest(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public SaleRequest(Map<String, Object> args)
    {
        super(args);
    }

    @Override
    public boolean accept(ShopCmdActor acceptingActor)
    {
        try {
            PlayerUtil.sellItem(shopId, buyer, seller, entry);
            ShopPlugin.sendNotification(seller, new SaleNotification(shopId, buyer, seller, entry));
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
        ShopPlugin.sendNotification(seller, rejection);
        rejectingActor.sendError("Offer rejected");
        return true;
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getBuyer().equals(sender)) {
            return String.format("%s wants to sell you %s for %s.",
                Format.username(seller), entry.getFormattedName(), entry.getFormattedSellPrice()
            );
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s wants to sell %s to %s for %s.",
            Format.username(seller), entry.getFormattedName(), Format.username2(buyer), entry.getFormattedSellPrice()
        );
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
