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
import tbax.baxshops.commands.ShopCmdActor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuyClaim implements Claimable
{
    private BaxEntry entry;
    private OfflinePlayer buyer;
    private OfflinePlayer seller;
    private UUID shopId;

    public BuyClaim(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer;
        this.seller = seller;
        this.entry = entry.clone();
    }

    public BuyClaim(Map<String, Object> args)
    {
        shopId = UUID.fromString((String)args.get("shopId"));
        buyer = (OfflinePlayer)args.get("buyer");
        seller = (OfflinePlayer)args.get("seller");
        entry = (BaxEntry)args.get("entry");
    }

    public OfflinePlayer getSeller()
    {
        return seller;
    }

    public UUID getShopId()
    {
        return shopId;
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
        return false;
    }

    @Override
    public String getMessage(CommandSender sender)
    {
        return null;
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

    public static BuyClaim deserialize(Map<String, Object> args)
    {
        return new BuyClaim(args);
    }

    public static BuyClaim valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
