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
package org.tbax.baxshops.serialization;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface UpgradeableSerializable extends ConfigurationSerializable
{
    @Deprecated
    default void upgrade00300(@NotNull SafeMap map)
    {
        SerializationException.throwVersionException();
    }

    @Deprecated
    default void upgrade00400(@NotNull SafeMap map)
    {
        upgrade00300(map);
    }

    @Deprecated
    default void upgrade00410(@NotNull SafeMap map)
    {
        upgrade00400(map);
    }

    @Deprecated
    default void upgrade00411(@NotNull SafeMap map)
    {
        upgrade00410(map);
    }

    @Deprecated
    default void upgrade00420(@NotNull SafeMap map)
    {
        upgrade00411(map);
    }

    @Deprecated
    default void upgrade00421(@NotNull SafeMap map)
    {
        UpgradeableSerialization.deserialize(this, map);
    }

    @Deprecated
    default void upgrade00422(@NotNull SafeMap map)
    {
        upgrade00421(map);
    }

    default void upgrade00450(@NotNull SafeMap map)
    {
        upgrade00422(map);
    }

    @Override
    default @NotNull Map<String, Object> serialize()
    {
        return UpgradeableSerialization.serialize(this);
    }
}
