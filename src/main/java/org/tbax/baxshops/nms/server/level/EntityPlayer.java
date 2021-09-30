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
package org.tbax.baxshops.nms.server.level;

import org.tbax.baxshops.nms.server.network.PlayerConnection;
import org.tbax.baxshops.nms.RuntimeObject;

import java.lang.reflect.Field;

public final class EntityPlayer extends RuntimeObject
{
    private static Field bField;
    public PlayerConnection playerConnection;

    @Override
    public String __pkg_name()
    {
        return "net.minecraft.server.level";
    }

    private Object runtimeObject;

    public EntityPlayer(Object runtimeObject) throws ReflectiveOperationException
    {
        this.runtimeObject = runtimeObject;
        if (bField == null) {
            bField = __class().getField("b");
        }
        playerConnection = new PlayerConnection(bField.get(runtimeObject));
    }

    @Override
    public Object __object()
    {
        return runtimeObject;
    }
}
