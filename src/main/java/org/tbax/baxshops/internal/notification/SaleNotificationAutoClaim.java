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
package org.tbax.baxshops.internal.notification;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.internal.serialization.StateLoader;
import org.tbax.baxshops.notification.Notification;

import java.util.Map;

@Deprecated
public class SaleNotificationAutoClaim implements DeprecatedNote, ConfigurationSerializable
{
    private SaleNotificationAuto note;

    public SaleNotificationAutoClaim(Map<String, Object> args)
    {
        note = new SaleNotificationAuto(args);
    }

    @Override
    public @NotNull SaleClaim getNewNote(StateLoader stateLoader)
    {
        return new SaleClaim(BaxShop.DUMMY_UUID,
                stateLoader.getPlayerSafe(null, note.getBuyer()),
                stateLoader.getPlayerSafe(null, note.getSeller()),
                note.getEntry()
        );
    }

    @Override
    public @NotNull Class<? extends Notification> getNewNoteClass()
    {
        return SaleClaim.class;
    }

    @Override
    public @NotNull Map<String, Object> serialize()
    {
        throw new UnsupportedOperationException();
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
