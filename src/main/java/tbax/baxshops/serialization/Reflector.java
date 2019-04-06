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

import tbax.baxshops.ShopPlugin;
import tbax.baxshops.notification.UpgradeableNote;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class Reflector
{
    private static final NumberFormat verFormat = new DecimalFormat("000.00");

    private Reflector()
    {
    }

    public static String getVersionString(double ver)
    {
        return verFormat.format(ver).replace(".", "");
    }

    public static Class<?> getClass(String pkg, String cls) throws ReflectiveOperationException
    {
        return Class.forName(pkg + "." + cls);
    }

    public static StateLoader getStateLoader(ShopPlugin plugin, double ver) throws ReflectiveOperationException
    {
        String verStr = getVersionString(ver);
        Class<?> stateClass = getClass("tbax.baxshops.serialization.states", "State_" + verStr);
        return (StateLoader)stateClass.getConstructor(ShopPlugin.class).newInstance(plugin);
    }

    public static Method getDeserializer(Class<? extends UpgradeableNote> cls) throws ReflectiveOperationException
    {
        String verStr = getVersionString(SavedState.getLoadedState());
        return cls.getMethod("deserialize" + verStr, SafeMap.class);
    }
}
