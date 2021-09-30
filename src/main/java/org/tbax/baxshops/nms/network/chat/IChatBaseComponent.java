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
package org.tbax.baxshops.nms.network.chat;

import org.tbax.baxshops.nms.RuntimeObject;

import java.lang.reflect.Method;

public final class IChatBaseComponent extends RuntimeObject
{
    private Object runtimeObject;

    public IChatBaseComponent(Object runtimeObject)
    {
        this.runtimeObject = runtimeObject;
    }

    @Override
    public Object __object()
    {
        return runtimeObject;
    }

    @Override
    public String __pkg_name()
    {
        return "net.minecraft.network.chat";
    }

    private static Method getStringMethod = null;
    public String getString() throws ReflectiveOperationException
    {
        if (getStringMethod == null) {
            getStringMethod = __method("getString");
        }
        return (String)getStringMethod.invoke(runtimeObject);
    }

    public static class ChatSerializer extends RuntimeObject
    {
        @Override
        public String __pkg_name()
        {
            return "net.minecraft.network.chat";
        }

        private static final String RUNTIME_CLASS_NAME = "net.minecraft.network.chat.IChatBaseComponent$ChatSerializer";
        private static Method aMethod = null;

        public static IChatBaseComponent a(String text) throws ReflectiveOperationException
        {
            Class<?> runtimeClass = __class(RUNTIME_CLASS_NAME);
            if (aMethod == null) {
                aMethod = runtimeClass.getMethod("a", String.class);
            }
            return new IChatBaseComponent(aMethod.invoke(runtimeClass, text));
        }

        @Override
        public String __class_name()
        {
            return RUNTIME_CLASS_NAME;
        }

        @Override
        public Object __object()
        {
            return null;
        }
    }
}
