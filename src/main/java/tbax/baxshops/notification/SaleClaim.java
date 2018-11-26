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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SaleClaim implements Claimable
{
    private OfflinePlayer buyer;
    private OfflinePlayer seller;
    private BaxEntry entry;
    private UUID shopId;

    public SaleClaim(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer;
        this.seller = seller;
        this.entry = entry.clone();
    }

    public SaleClaim(Map<String, Object> args)
    {
        shopId = UUID.fromString((String)args.get("shopId"));
        buyer = (OfflinePlayer)args.get("buyer");
        seller = (OfflinePlayer)args.get("seller");
        entry = (BaxEntry) args.get("entry");
    }

    public OfflinePlayer getSeller()
    {
        return seller;
    }

    public OfflinePlayer getBuyer()
    {
        return buyer;
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
        if (sender == null || !buyer.equals(sender)) {
            return String.format("%s has sold %s to %s for %s",
                Format.username(seller.getName()), entry.getFormattedName(), Format.username2(buyer.getName()),entry.getFormattedSellPrice()
                );
        }
        else {
            return String.format("You bought %s from %s for %s",
                entry.getFormattedName(), Format.username(buyer.getName()), entry.getFormattedSellPrice()
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
