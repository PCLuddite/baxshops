/*
 * Copyright (C) 2013-2019 Timothy Baxendale
 * Portions derived from Shops Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus.
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
package org.tbax.baxshops.serialization.qs;

import org.bukkit.OfflinePlayer;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.serialization.StoredPlayer;
import qs.shops.Shop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NathanConverter
{
    private static Map<Shop, BaxShop> shopMap = new HashMap<>();
    private static Map<String, StoredPlayer> playerMap = new HashMap<>();

    public static UUID registerShop(Shop shop)
    {
        BaxShop baxShop = shopMap.get(shop);
        if (baxShop == null) {
            baxShop = BaxShop.fromNathan(shop);
            shopMap.put(shop, baxShop);
        }
        return baxShop.getId();
    }

    public static OfflinePlayer registerPlayer(String name)
    {
        StoredPlayer player = playerMap.get(name);
        if (player == null) {
            player = new StoredPlayer(name);
            playerMap.put(name, player);
        }
        return player;
    }
}
