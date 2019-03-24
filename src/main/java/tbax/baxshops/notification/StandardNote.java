/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.StateConversion;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class StandardNote implements Notification
{
    protected BaxEntry entry;
    protected UUID shopId;
    protected UUID buyer;
    protected UUID seller;

    public StandardNote(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer.getUniqueId();
        this.seller = seller.getUniqueId();
        this.entry = new BaxEntry(entry);
    }

    public StandardNote(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer;
        this.seller = seller;
        this.entry = new BaxEntry(entry);
    }

    public StandardNote(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        if (isLegacy(map)) {
            deserializeLegacy(map);
        }
        else {
            deserialize(map);
        }
    }

    protected boolean isLegacy(SafeMap map)
    {
        return map.get("shopId") instanceof Number;
    }

    protected void deserializeLegacy(SafeMap map)
    {
        buyer = StateConversion.getPlayerId(map.getString("buyer"));
        seller = StateConversion.getPlayerId(map.getString("seller"));
        shopId = StateConversion.getShopId(map.getLong("shopId"));
        entry = map.getBaxEntry("entry");
    }

    protected void deserialize(SafeMap map)
    {
        entry = map.getBaxEntry("entry");
        buyer = map.getUUID("buyer");
        seller = map.getUUID("seller");
        shopId = map.getUUID("shopId");
    }

    public @NotNull OfflinePlayer getBuyer()
    {
        return ShopPlugin.getOfflinePlayer(buyer);
    }

    public @NotNull OfflinePlayer getSeller()
    {
        return ShopPlugin.getOfflinePlayer(seller);
    }

    public @NotNull BaxEntry getEntry()
    {
        return entry;
    }

    public @NotNull UUID getShopId()
    {
        return shopId;
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("entry", entry);
        args.put("buyer", getBuyer().getUniqueId().toString());
        args.put("seller", getSeller().getUniqueId().toString());
        args.put("shopId", shopId.toString());
        return args;
    }
}
