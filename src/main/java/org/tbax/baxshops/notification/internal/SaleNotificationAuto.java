/*
 * Copyright (C) Timothy Baxendale
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
package org.tbax.baxshops.notification.internal;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.notification.Notification;
import org.tbax.baxshops.serialization.SafeMap;
import org.tbax.baxshops.serialization.internal.StateLoader;
import org.tbax.baxshops.serialization.internal.states.State_00300;

import java.util.Map;

@Deprecated
public class SaleNotificationAuto implements DeprecatedNote, ConfigurationSerializable
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
    public @NotNull SaleNotification getNewNote(StateLoader stateLoader)
    {
        return new SaleNotification(BaxShop.DUMMY_UUID,
                ((State_00300)stateLoader).getPlayer(buyer),
                ((State_00300)stateLoader).getPlayer(seller),
                entry);
    }

    @Override
    public @NotNull Class<? extends Notification> getNewNoteClass()
    {
        return SaleNotification.class;
    }

    @Override
    public @NotNull Map<String, Object> serialize()
    {
        throw new UnsupportedOperationException();
    }

    public static SaleNotificationAuto deserialize(Map<String, Object> args)
    {
        return new SaleNotificationAuto(args);
    }

    public static SaleNotificationAuto valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    public String getBuyer()
    {
        return buyer;
    }

    public String getSeller()
    {
        return seller;
    }

    public BaxEntry getEntry()
    {
        return entry;
    }
}
