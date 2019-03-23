/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.StateConversion;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.Map;

@Deprecated
public class SellRequest implements DeprecatedNote
{
    private String seller;
    private String buyer;
    private long shopId;
    private long expirationDate;
    private BaxEntry entry;

    public SellRequest(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        seller = map.getString("seller");
        buyer = map.getString("buyer");
        shopId = map.getInteger("shop");
        expirationDate = map.getLong("expires");
    }

    @Override
    public @NotNull SaleRequest getNewNote()
    {
        return new SaleRequest(StateConversion.getShop(shopId).getId(),
            getBuyer(),
            getSeller(),
            entry);
    }

    public OfflinePlayer getBuyer()
    {
        return buyer == null ? StoredPlayer.ERROR : ShopPlugin.getOfflinePlayer(buyer).get(0);
    }

    public OfflinePlayer getSeller()
    {
        return seller == null ? StoredPlayer.ERROR : ShopPlugin.getOfflinePlayer(seller).get(0);
    }

    @Override
    public @NotNull Class<? extends Notification> getNewNoteClass()
    {
        return SaleRequest.class;
    }

    @Override
    public Map<String, Object> serialize()
    {
        throw new NotImplementedException();
    }

    public static SellRequest deserialize(Map<String, Object> args)
    {
        return new SellRequest(args);
    }

    public static SellRequest valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
