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
package org.tbax.baxshops.notification;

import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;

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
