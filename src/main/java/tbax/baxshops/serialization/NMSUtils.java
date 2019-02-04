package tbax.baxshops.serialization;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.Format;
import tbax.baxshops.Main;

/**
 * @author iFamasssxD Modified by Timothy Baxendale
 */
public class NMSUtils
{
    static String version = "";

    static {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String mcVersion = name.substring(name.lastIndexOf('.') + 1);
        version = mcVersion + ".";
    }

    public static Class<?> getCraftClass(String ClassName)
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
    
    public static Class<?> getNMSClass(String ClassName)
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
            return (String)getName.invoke(nmsCopy);
        } catch (Exception e) {
            Main.getLog().warning("Could not get item name for " + stack.getType());
        }
        return stack.toString().replace("ItemStack", "");
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

    public static Method getMethod(Class<?> cl, String method) throws Exception
    {
        return cl.getDeclaredMethod(method);
    }

    public static Method getMethod(Class<?> cl, String method, Class<?>[] args) throws Exception
    {
        return cl.getDeclaredMethod(method, args);
    }

    public static Field getField(Class<?> cl, String field_name)
    {
        try {
            Field field = cl.getDeclaredField(field_name);
            field.setAccessible(true);
            return field;
        } catch (SecurityException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }
}
