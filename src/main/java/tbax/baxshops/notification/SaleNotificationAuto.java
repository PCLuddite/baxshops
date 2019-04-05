/*
 * Copyright (C) 2013-2019 Timothy Baxendale
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.StoredPlayer;
import tbax.baxshops.serialization.states.State_30;

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
        return buyer == null ? StoredPlayer.ERROR : State_30.getPlayer(buyer);
    }

    public OfflinePlayer getSeller()
    {
        return seller == null ? StoredPlayer.ERROR : State_30.getPlayer(seller);
    }

    public BaxEntry getEntry()
    {
        return entry;
    }
}
