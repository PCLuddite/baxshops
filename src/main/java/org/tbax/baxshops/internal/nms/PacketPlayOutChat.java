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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public final class PacketPlayOutChat extends Packet
{
    private Object runtimeObject;

    private static Field aField = null;
    public IChatBaseComponent a;

    private static Constructor<?> ctor;
    public PacketPlayOutChat(IChatBaseComponent component) throws ReflectiveOperationException
    {
        a = component;
        if (ctor == null) {
            ctor = __class().getConstructor(component.__class());
        }
        runtimeObject = ctor.newInstance(a.__object());
    }

    @Override
    public Object __object() throws ReflectiveOperationException
    {
        if (aField == null) {
            aField = __class().getDeclaredField("a");
            aField.setAccessible(true);
        }
        aField.set(runtimeObject, a.__object());
        return runtimeObject;
    }
}
