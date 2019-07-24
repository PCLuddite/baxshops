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
package tbax.baxshops.serialization;

import tbax.baxshops.serialization.annotations.SerializeMethod;
import tbax.baxshops.serialization.annotations.SerializedAs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
        if (as != null) {
            return as.value();
        }
        return field.getName();
    }

    public <E extends UpgradeableSerializable> Object get(E obj) throws ReflectiveOperationException
    {
        Method getter = getGetter();
        if (getter == null)
            return field.get(obj);
        return getter.invoke(obj);
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
        if (m == null)
            return null;
        Class<?> current = clazz;
        do {
            try {
                Method method = current.getDeclaredMethod(m.getter());
                method.setAccessible(true);
                return method;
            }
            catch (NoSuchMethodException e) {
                current = clazz.getSuperclass();
            }
        }
        while (current != null && !Object.class.equals(current));
        throw new NoSuchMethodException();
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

    public Object putMap(SafeMap map, UpgradeableSerializable obj) throws ReflectiveOperationException
    {
        return getMapPutter().invoke(map, name(), get(obj));
    }
}
