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
import tbax.baxshops.Format;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.serialization.ItemNames;
import tbax.baxshops.serialization.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuyClaim implements Claimable
{
    private BaxEntry entry;
    private UUID buyer;
    private UUID seller;
    private UUID shopId;

    public BuyClaim(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer.getUniqueId();
        this.seller = seller.getUniqueId();
        this.entry = entry.clone();
    }

    public BuyClaim(Map<String, Object> args)
    {
        shopId = UUID.fromString((String)args.get("shopId"));
        buyer = UUID.fromString((String)args.get("buyer"));
        seller = UUID.fromString((String)args.get("seller"));
        entry = (BaxEntry)args.get("entry");
    }

    public OfflinePlayer getSeller()
    {
        return SavedData.getOfflinePlayer(seller);
    }

    public UUID getShopId()
    {
        return shopId;
    }

    public OfflinePlayer getBuyer()
    {
        return SavedData.getOfflinePlayer(buyer);
    }

    @Override
    public BaxEntry getEntry()
    {
        return entry;
    }

    @Override
    public boolean claim(ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public String getMessage(CommandSender sender)
    {
        if (getBuyer().equals(sender)) {
            return String.format("%s accepted %s's request to buy %s for %s.",
                Format.username(seller),
                Format.username2(buyer),
                entry.getFormattedName(),
                entry.getFormattedBuyPrice()
            );
        }
        else {
            return String.format("%s accepted your request to buy %s for %s.",
                Format.username(seller),
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
        args.put("buyer", buyer.toString());
        args.put("seller", seller.toString());
        args.put("entry", entry);
        return args;
    }

    public static BuyClaim deserialize(Map<String, Object> args)
    {
        return new BuyClaim(args);
    }

    public static BuyClaim valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
