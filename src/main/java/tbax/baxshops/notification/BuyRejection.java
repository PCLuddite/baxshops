/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.Format;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuyRejection implements Notification
{
    private OfflinePlayer seller;
    private OfflinePlayer buyer;
    private BaxEntry entry;
    private UUID shopId;

    public BuyRejection(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer;
        this.seller = seller;
        this.entry = entry.clone();
    }

    public BuyRejection(Map<String, Object> args)
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

    public BaxEntry getEntry()
    {
        return entry;
    }

    @Override
    public String getMessage(CommandSender sender)
    {
        if (sender == null || !sender.equals(seller)) {
            return String.format("%s " + ChatColor.RED + "rejected" + ChatColor.RESET + " %s's request to buy %s for %s.",
                Format.username(seller.getName()),
                Format.username2(buyer.getName()),
                entry.getFormattedName(),
                entry.getFormattedBuyPrice()
            );
        }
        else {
            return String.format("%s " + ChatColor.RED + "rejected" + ChatColor.RESET + " your request to buy %s for %s.",
                Format.username(seller.getName()),
                entry.getFormattedName(),
                entry.getFormattedBuyPrice()
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

    public static BuyRejection deserialize(Map<String, Object> args)
    {
        return new BuyRejection(args);
    }

    public static BuyRejection valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
