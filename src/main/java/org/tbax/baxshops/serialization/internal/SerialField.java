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
package org.tbax.baxshops.serialization.internal;

import org.tbax.baxshops.serialization.SafeMap;
import org.tbax.baxshops.serialization.annotations.SerializeMethod;
import org.tbax.baxshops.serialization.annotations.SerializedAs;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class SerialField
{
    private final Field field;
    private final Class<? extends UpgradeableSerializable> clazz;

    public SerialField(Class<? extends UpgradeableSerializable> clazz, Field f)
    {
        f.setAccessible(true);
        field = f;
        this.clazz = clazz;
    }

    public String name()
    {
        SerializedAs as = field.getAnnotation(SerializedAs.class);
        if (as != null)
            return as.value();
        return field.getName();
    }

    public <E extends UpgradeableSerializable> Object get(E obj) throws ReflectiveOperationException
    {
        Method getter = getGetter();
        if (getter == null)
            return field.get(obj);
        return getter.invoke(obj);
    }

    public <E extends UpgradeableSerializable> void set(E obj, Object value)
            throws ReflectiveOperationException
    {
        Method setter = getSetter();
        if (setter == null) {
            if (field.getType().isArray() && value != null) {
                List list = (List)value;
                Object arr = Array.newInstance(field.getType().getComponentType(), list.size());
                for(int x = 0; x < list.size(); ++x) {
                    Array.set(arr, x, list.get(x));
                }
                field.set(obj, arr);
            }
            else {
                field.set(obj, value);
            }
        }
        else {
            setter.invoke(obj, value);
        }
    }

    public Class<?> getType() throws ReflectiveOperationException
    {
        Method getter = getGetter();
        if (getter == null)
            return field.getType();
        return getter.getReturnType();
    }

    public Method getGetter() throws NoSuchMethodException
    {
        SerializeMethod m = field.getAnnotation(SerializeMethod.class);
        if (m == null || m.getter() == null || "".equals(m.getter()))
            return null;
        return findMethod(clazz, m.getter());
    }

    public Method getSetter() throws NoSuchMethodException
    {
        SerializeMethod m = field.getAnnotation(SerializeMethod.class);
        if (m == null || m.setter() == null || "".equals(m.setter()))
            return null;
        return findMethod(clazz, m.setter());
    }

    private static Method findMethod(Class<?> clazz, String methodName) throws NoSuchMethodException
    {
        Class<?> current = clazz;
        do {
            try {
                Method method;
                try {
                    method = current.getMethod(methodName);
                }
                catch (NoSuchMethodException e) {
                    method = current.getDeclaredMethod(methodName);
                    method.setAccessible(true);
                }
                return method;
            }
            catch (NoSuchMethodException e) {
                current = clazz.getSuperclass();
            }
        }
        while (current != null);
        throw new NoSuchMethodException(methodName);
    }

    public Method getMapPutter() throws ReflectiveOperationException
    {
        try {
            return SafeMap.class.getDeclaredMethod("put", String.class, getType());
        }
        catch (NoSuchMethodException e) {
            return SafeMap.class.getDeclaredMethod("put", String.class, Object.class);
        }
    }

    public Method getMapGetter() throws ReflectiveOperationException
    {
        try {
            String type;
            if (field.getType().isPrimitive()) {
                type = Array.get(Array.newInstance(field.getType(),1),0).getClass().getSimpleName();
            }
            else {
                type = field.getType().getSimpleName();
            }
            return SafeMap.class.getDeclaredMethod("get" + type, String.class, field.getType());
        }
        catch (NoSuchMethodException e) {
            return SafeMap.class.getDeclaredMethod("get", String.class, Object.class);
        }
    }

    public Object putMap(SafeMap map, UpgradeableSerializable obj) throws ReflectiveOperationException
    {
        return getMapPutter().invoke(map, name(), get(obj));
    }

    public void getMap(SafeMap map, UpgradeableSerializable obj) throws ReflectiveOperationException
    {
        set(obj, getMapGetter().invoke(map, name(), field.get(obj)));
    }
}
