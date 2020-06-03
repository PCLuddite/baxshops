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

import java.lang.reflect.Method;

public final class PlayerConnection extends NmsObject
{
    private Object runtimeObject;

    public PlayerConnection(Object runtimeObject)
    {
        this.runtimeObject = runtimeObject;
    }

    private static Method sendPacketMethod = null;
    public void sendPacket(Packet packet) throws ReflectiveOperationException
    {
        if (sendPacketMethod == null) {
            sendPacketMethod = __method("sendPacket",
                    __class(__pkg_name() + ".Packet"));
        }
        sendPacketMethod.invoke(runtimeObject, packet.__object());
    }

    @Override
    public Object __object()
    {
        return runtimeObject;
    }
}
