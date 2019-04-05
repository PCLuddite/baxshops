/*
 * Copyright (C) 2013-2019 Timothy Baxendale
 * Portions derived from HorseModifier Copyright (c) 2013-2015 iFamasssxD.
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

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("SameParameterValue")
public final class NMSUtils
{
    private static String version = "";

    static {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String mcVersion = name.substring(name.lastIndexOf('.') + 1);
        version = mcVersion + ".";
    }

    private NMSUtils()
    {
    }

    private static Class<?> getCraftClass(String ClassName)
    {
        String className = "org.bukkit.craftbukkit." + version + ClassName;
        Class<?> c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return c;
    }
    
    private static Class<?> getNMSClass(String ClassName)
    {
        String className = "net.minecraft.server." + version + ClassName;
        Class<?> c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return c;
    }

    public static String getItemName(ItemStack stack)
    {
        try {
            Method asNMSCopy = getMethod(getCraftClass("inventory.CraftItemStack"), "asNMSCopy", new Class<?>[] { ItemStack.class });
            Object nmsCopy = asNMSCopy.invoke(null, stack);
            Method getName = getMethod(getNMSClass("ItemStack"), "getName");
            try {
                return (String) getName.invoke(nmsCopy);
            }
            catch(ClassCastException e) {
                Object msg = getName.invoke(nmsCopy);
                return (String)msg.getClass().getMethod("getText").invoke(msg);
            }
        } catch (Exception e) {
            //ShopPlugin.getLogger().warning("Could not get item name for " + stack.getType());
            return stack.getType().toString();
        }
    }

    public static Object getHandle(Entity entity) throws Exception
    {
        Object nms_entity = null;
        Method entity_getHandle = getMethod(entity.getClass(), "getHandle");
        try {
            nms_entity = entity_getHandle.invoke(entity);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return nms_entity;
    }

    private static Method getMethod(Class<?> cl, String method) throws Exception
    {
        return cl.getDeclaredMethod(method);
    }

    private static Method getMethod(Class<?> cl, String method, Class<?>[] args) throws Exception
    {
        return cl.getDeclaredMethod(method, args);
    }

}
