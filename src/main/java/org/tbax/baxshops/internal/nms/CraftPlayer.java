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
package org.tbax.baxshops.internal.nms;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public final class CraftPlayer extends CraftObject
{
    private Player player;

    public CraftPlayer(Player player)
    {
        this.player = player;
    }

    @Override
    protected String getRuntimePackageName()
    {
        return super.getRuntimePackageName() + ".entity";
    }

    private static Method getHandleMethod;
    public EntityPlayer getHandle() throws ReflectiveOperationException
    {
        if (getHandleMethod == null) {
            getHandleMethod = getRuntimeMethod("getHandle");
        }
        return new EntityPlayer(getHandleMethod.invoke(player));
    }

    @Override
    public Object getRuntimeObject()
    {
        return player;
    }
}
