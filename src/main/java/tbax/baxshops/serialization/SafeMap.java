/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tbax.baxshops.BaxEntry;

import java.util.*;

@SuppressWarnings("unused")
public class SafeMap implements Map<String, Object>
{
    private Map<String, Object> argMap;

    public SafeMap(Map<String, Object> map)
    {
        argMap = map;
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

    @Override
    public int size()
    {
        return argMap.size();
    }

    @Override
    public boolean isEmpty()
    {
        return argMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return argMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return argMap.containsValue(value);
    }

    @Override
    public Object get(Object key)
    {
        return argMap.get(key);
    }

    @Override
    public @Nullable Object put(String key, Object value)
    {
        return argMap.put(key, value);
    }

    @Override
    public Object remove(Object key)
    {
        return argMap.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ?> m)
    {
        argMap.putAll(m);
    }

    @Override
    public void clear()
    {
        argMap.clear();
    }

    @Override
    public @NotNull Set<String> keySet()
    {
        return argMap.keySet();
    }

    @Override
    public @NotNull Collection<Object> values()
    {
        return argMap.values();
    }

    @Override
    public @NotNull Set<Entry<String, Object>> entrySet()
    {
        return argMap.entrySet();
    }

    public BaxEntry getBaxEntry(String key)
    {
        return getBaxEntry(key, null);
    }

    public BaxEntry getBaxEntry(String key, BaxEntry defaultValue)
    {
        try {
            return (BaxEntry)getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public long getLong(String key)
    {
        return getLong(key, 0);
    }

    public long getLong(String key, long defaultValue)
    {
        try {
            return (long)getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            try {
                return (int)getOrDefault(key, defaultValue);
            }
            catch (ClassCastException e1) {
                return defaultValue;
            }
        }
    }
}
