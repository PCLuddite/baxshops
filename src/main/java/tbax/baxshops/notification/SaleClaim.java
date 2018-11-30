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
import tbax.baxshops.serialization.StoredData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SaleClaim implements Claimable
{
    private UUID buyer;
    private UUID seller;
    private BaxEntry entry;
    private UUID shopId;

    public SaleClaim(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer.getUniqueId();
        this.seller = seller.getUniqueId();
        this.entry = new BaxEntry(entry);
    }

    public SaleClaim(Map<String, Object> args)
    {
        shopId = UUID.fromString((String)args.get("shopId"));
        buyer = UUID.fromString((String)args.get("buyer"));
        seller = UUID.fromString((String)args.get("seller"));
        entry = (BaxEntry) args.get("entry");
    }

    public OfflinePlayer getSeller()
    {
        return StoredData.getOfflinePlayer(seller);
    }

    public OfflinePlayer getBuyer()
    {
        return StoredData.getOfflinePlayer(buyer);
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
        if (getBuyer().equals(sender)) {
            return String.format("You bought %s from %s for %s",
                entry.getFormattedName(), Format.username(buyer), entry.getFormattedSellPrice()
            );
        }
        else {
            return String.format("%s has sold %s to %s for %s",
                Format.username(seller), entry.getFormattedName(), Format.username2(buyer), entry.getFormattedSellPrice()
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
        return null;
    }

    public static SaleClaim deserialize(Map<String, Object> args)
    {
        return new SaleClaim(args);
    }

    public static SaleClaim valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
