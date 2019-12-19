/*
 * Copyright (C)Timothy Baxendale
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
package org.tbax.baxshops.internal.notification;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.notification.Notification;
import org.tbax.baxshops.serialization.SafeMap;
import org.tbax.baxshops.internal.serialization.StateLoader;
import org.tbax.baxshops.internal.serialization.states.StateLoader_00300;

import java.util.Map;

@Deprecated
public class SellRequest implements DeprecatedNote, ConfigurationSerializable
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
        entry = map.getBaxEntry("entry");
        expirationDate = map.getLong("expires");
    }

    @Override
    public @NotNull SaleRequest getNewNote(StateLoader stateLoader)
    {
        return new SaleRequest(((StateLoader_00300)stateLoader).getShopId(shopId),
                stateLoader.getPlayerSafe(null, buyer),
                stateLoader.getPlayerSafe(null, seller),
                entry);
    }

    public String getBuyer()
    {
        return buyer;
    }

    public String getSeller()
    {
        return seller;
    }

    @Override
    public @NotNull Class<? extends Notification> getNewNoteClass()
    {
        return SaleRequest.class;
    }

    @Override
    public @NotNull Map<String, Object> serialize()
    {
        throw new UnsupportedOperationException();
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