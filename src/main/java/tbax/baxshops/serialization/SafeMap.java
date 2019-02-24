/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization;

import org.bukkit.inventory.ItemStack;

import java.util.*;

@SuppressWarnings("unused")
public class SafeMap extends HashMap<String, Object>
{
    public SafeMap(Map<String, Object> hashMap)
    {
        putAll(hashMap);
    }

    public boolean getBoolean(String key)
    {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue)
    {
        try {
            return (boolean)getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public int getInteger(String key)
    {
        return getInteger(key, 0);
    }

    public int getInteger(String key, int defaultValue)
    {
        try {
            return (int)getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public String getString(String key)
    {
        return getString(key, "");
    }

    public String getString(String key, String defaultValue)
    {
        try {
            return (String)getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public double getDouble(String key)
    {
        return getDouble(key, 0);
    }

    public double getDouble(String key, double defaultValue)
    {
        try {
            return (double)getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public UUID getUUID(String key)
    {
        return getUUID(key, null);
    }

    public UUID getUUID(String key, UUID defaultValue)
    {
        try {
            return UUID.fromString(getString(key));
        }
        catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    public <E> List<E> getList(String key)
    {
        return getList(key, new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public <E> List<E> getList(String key, List<E> defaultValue)
    {
        try {
            return (List<E>)getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public ItemStack getItemStack(String key)
    {
        return getItemStack(key, null);
    }

    @SuppressWarnings("unchecked")
    public ItemStack getItemStack(String key, ItemStack defaultValue)
    {
        try {
            return ItemStack.deserialize((Map)getOrDefault(key, defaultValue));
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }
}
