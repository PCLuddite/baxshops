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

import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class RuntimeObject
{
    public static final String MINECRAFT_VERSION = Bukkit.getServer().getClass().getPackage().getName()
            .replace(".", ",").split(",")[3];

    private final static Map<String, Class<?>> classCache = new HashMap<>();

    protected abstract String getRuntimePackageName();

    protected String getRuntimeClassName()
    {
        Class<? extends RuntimeObject> cls = getClass();
        return cls.getName().substring(cls.getName().lastIndexOf('.') + 1);
    }

    public final Class<?> getRuntimeClass() throws ReflectiveOperationException
    {
        return getRuntimeClass(getRuntimePackageName() + "." + getRuntimeClassName());
    }

    public static Class<?> getRuntimeClass(String className) throws ReflectiveOperationException
    {
        Class<?> cls = classCache.get(className);
        if (cls == null) {
            classCache.put(className, cls = Class.forName(className));
        }
        return cls;
    }

    protected final Method getRuntimeMethod(String name, Class<?>... parameterTypes) throws ReflectiveOperationException
    {
        return getRuntimeClass().getDeclaredMethod(name, parameterTypes);
    }

    public abstract Object getRuntimeObject() throws ReflectiveOperationException;
}
