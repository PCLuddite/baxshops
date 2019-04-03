/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.SavedState;
import tbax.baxshops.serialization.StoredPlayer;
import tbax.baxshops.serialization.states.State_30;
import tbax.baxshops.serialization.states.State_40;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public abstract class StandardNote implements UpgradeableNote
{
    protected BaxEntry entry;
    protected UUID shopId;
    protected UUID buyer;
    protected UUID seller;
    protected Date date;

    public StandardNote(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this(shopId, buyer.getUniqueId(), seller.getUniqueId(), entry);
    }

    public StandardNote(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer;
        this.seller = seller;
        this.entry = new BaxEntry(entry);
        this.date = new Date();
    }

    public StandardNote(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        if (SavedState.getLoadedState() == State_40.VERSION) {
            deserialize40(map);
        }
        else if (SavedState.getLoadedState() == State_30.VERSION) {
            deserialize30(map);
        }
        else {
            deserialize(map);
        }
    }

    @Deprecated
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
        date = map.getDate("date");
    }

    @Override
    public @Nullable Date getSentDate()
    {
        return date;
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
        SafeMap args = new SafeMap();
        args.put("entry", entry);
        args.put("buyer", getBuyer());
        args.put("seller", getSeller());
        args.put("shopId", shopId == null ? BaxShop.DUMMY_UUID : shopId);
        args.put("date", date);
        return args;
    }
}
