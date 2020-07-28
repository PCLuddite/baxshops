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
package org.tbax.baxshops.nms;

import java.lang.reflect.Method;

public final class ChatMessage extends NmsObject
{
    private Object runtimeObject;

    public ChatMessage(Object runtimeObject)
    {
        this.runtimeObject = runtimeObject;
    }

    private static Method getTextMethod = null;
    public String getText() throws ReflectiveOperationException
    {
        if (getTextMethod == null) {
            getTextMethod = __method("getText");
        }
        return (String)getTextMethod.invoke(runtimeObject);
    }

    @Override
    public Object __object()
    {
        return runtimeObject;
    }
}
