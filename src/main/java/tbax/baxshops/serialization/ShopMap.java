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

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tbax.baxshops.BaxShop;

import java.util.*;

public class ShopMap implements Map<UUID, BaxShop>
{
    private final Map<UUID, BaxShop> shops = new HashMap<>();
    private final Map<Location, UUID> locations = new HashMap<>();
    private final Map<String, UUID> shortId2s = new HashMap<>();
    private final Map<String, UUID> shortIds = new HashMap<>();

    public ShopMap()
    {
    }

    public ShopMap(Collection<? extends BaxShop> shops)
    {
        for(BaxShop shop : shops) {
            put(shop.getId(), shop);
        }
    }

    public @Nullable BaxShop getShopByLocation(@NotNull Location loc)
    {
        UUID id = locations.get(loc);
        if (id == null)
            return null;
        return shops.get(id);
    }

    @Deprecated
    public @Nullable BaxShop getShopByShortId(@NotNull String shortId)
    {
        UUID id = shortIds.get(shortId);
        if (id == null)
            return null;
        return shops.get(id);
    }

    public @Nullable BaxShop getShopByShortId2(@NotNull String shortId2)
    {
        UUID id = shortId2s.get(shortId2);
        if (id == null)
            return null;
        return shops.get(id);
    }

    @Override
    public int size()
    {
        return shops.size();
    }

    @Override
    public boolean isEmpty()
    {
        return shops.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return shops.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return shops.containsValue(value);
    }

    @Override
    public BaxShop get(Object key)
    {
        return shops.get(key);
    }

    @Override
    public @Nullable BaxShop put(UUID key, BaxShop value)
    {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        if (!key.equals(value.getId()))
            throw new IllegalArgumentException();

        for (Location location : value.getLocations()) {
            locations.put(location, key);
        }
        shortId2s.put(getShortId(value), value.getId());
        if (value.getShortId() != null) {
            shortIds.put(value.getShortId(), value.getId());
        }
        return shops.put(key, value);
    }

    @Override
    public BaxShop remove(Object key)
    {
        Objects.requireNonNull(key);
        BaxShop shop = shops.remove(key);
        if (shop != null) {
            for (Location location : shop.getLocations()) {
                locations.remove(location);
            }
        }
        return shop;
    }

    @Override
    public void putAll(@NotNull Map<? extends UUID, ? extends BaxShop> m)
    {
        for(Map.Entry<? extends UUID, ? extends BaxShop> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear()
    {
        shops.clear();
        locations.clear();
        shortIds.clear();
        shortId2s.clear();
    }

    @Override
    public @NotNull Set<UUID> keySet()
    {
        return shops.keySet();
    }

    @Override
    public @NotNull Collection<BaxShop> values()
    {
        return shops.values();
    }

    @Override
    public @NotNull Set<Entry<UUID, BaxShop>> entrySet()
    {
        return shops.entrySet();
    }

    public void addLocation(UUID id, Location loc)
    {
        BaxShop shop = shops.get(id);
        if (shop == null)
            throw new NullPointerException();
        locations.put(loc, id);
        shop.addLocation(loc);
    }

    public void removeLocation(UUID id, Location loc)
    {
        BaxShop shop = shops.get(id);
        if (shop == null)
            throw new NullPointerException();
        shop.removeLocation(loc);
        locations.remove(loc);
        if (shop.getLocations().isEmpty()) {
            remove(shop.getId());
        }
    }

    private String getShortId(BaxShop shop)
    {
        UUID longId;
        String id = shop.getShortId2();
        while ((longId = shortId2s.get(id)) != null && !longId.equals(shop.getId())) {
            id = UUID.randomUUID().toString();
            int n = id.lastIndexOf('-') + 1;
            if (n < id.length())
                shop.setShortId2(id.substring(n));
            id = shop.getShortId2();
        }
        return id;
    }
}
