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
import tbax.baxshops.BaxShop;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.StoredData;
import tbax.baxshops.serialization.StoredPlayer;
import tbax.baxshops.serialization.states.State_30;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class StandardNote implements UpgradeableNote
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
        if (StoredData.getLoadedState() == State_30.VERSION) {
            deserialize30(map);
        }
        else {
            deserialize(map);
        }
    }

    public void deserialize30(@NotNull SafeMap map)
    {
        buyer = State_30.getPlayerId(map.getString("buyer", StoredPlayer.DUMMY_NAME));
        seller = State_30.getPlayerId(map.getString("seller", StoredPlayer.DUMMY_NAME));
        shopId = State_30.getShopId(map.getLong("shopId"));
        entry = map.getBaxEntry("entry");
    }

    public void deserialize(@NotNull SafeMap map)
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
        args.put("shopId", (shopId == null ? BaxShop.DUMMY_UUID : shopId).toString());
        return args;
    }
}
