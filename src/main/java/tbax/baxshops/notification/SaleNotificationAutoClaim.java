/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.notification;

import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tbax.baxshops.BaxShop;

import java.util.Map;

@Deprecated
public class SaleNotificationAutoClaim implements DeprecatedNote
{
    private SaleNotificationAuto note;

    public SaleNotificationAutoClaim(Map<String, Object> args)
    {
        note = new SaleNotificationAuto(args);
    }

    @Override
    public @NotNull SaleClaim getNewNote()
    {
        return new SaleClaim(BaxShop.DUMMY_UUID, note.getBuyer(), note.getSeller(), note.getEntry());
    }

    @Override
    public @NotNull Class<? extends Notification> getNewNoteClass()
    {
        return SaleClaim.class;
    }

    @Override
    public Map<String, Object> serialize()
    {
        throw new NotImplementedException();
    }

    public static SaleNotificationAutoClaim deserialize(Map<String, Object> args)
    {
        return new SaleNotificationAutoClaim(args);
    }

    public static SaleNotificationAutoClaim valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
