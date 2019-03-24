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
import tbax.baxshops.BaxShop;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.StateConversion;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.Map;

@Deprecated
public class SaleNotificationAuto implements DeprecatedNote
{
    private String buyer;
    private String seller;
    private BaxEntry entry;

    public SaleNotificationAuto(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        this.seller = map.getString("seller");
        this.entry = map.getBaxEntry("entry");
        this.buyer = map.getString("buyer");
    }

    @Override
    public @NotNull SaleNotification getNewNote()
    {
        return new SaleNotification(BaxShop.DUMMY_UUID, getBuyer(), getSeller(), entry);
    }

    @Override
    public @NotNull Class<? extends Notification> getNewNoteClass()
    {
        return SaleNotification.class;
    }

    @Override
    public Map<String, Object> serialize()
    {
        throw new NotImplementedException();
    }

    public static SaleNotificationAuto deserialize(Map<String, Object> args)
    {
        return new SaleNotificationAuto(args);
    }

    public static SaleNotificationAuto valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    public OfflinePlayer getBuyer()
    {
        return buyer == null ? StoredPlayer.ERROR : StateConversion.getPlayer(buyer);
    }

    public OfflinePlayer getSeller()
    {
        return seller == null ? StoredPlayer.ERROR : StateConversion.getPlayer(seller);
    }

    public BaxEntry getEntry()
    {
        return entry;
    }
}
