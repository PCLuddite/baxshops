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
package org.tbax.baxshops.internal.serialization.states;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.notification.Notification;
import org.tbax.baxshops.serialization.StoredPlayer;

import java.util.*;

public final class StateLoader_00481 extends StateLoader_00480
{
    public static final double VERSION = 4.81;

    public StateLoader_00481(ShopPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public @NotNull Collection<StoredPlayer> buildPlayers(@NotNull FileConfiguration state)
    {
        Map<UUID, StoredPlayer> players = new HashMap<>();
        if (!state.isList("players")) {
            return players.values();
        }
        for(Object o : state.getList("players")) {
            if (o instanceof StoredPlayer) {
                StoredPlayer player = (StoredPlayer)o;
                StoredPlayer oldPlayer = players.put(player.getUniqueId(), player);
                if (oldPlayer != null && oldPlayer != player) {
                    ShopPlugin.logWarning(String.format("Found multiple players with id %s. These will be consolidated into one player.", player.getUniqueId()));
                    if (oldPlayer.isSpecial()) { // special players take precedence
                        StoredPlayer tmp = player;
                        player = oldPlayer;
                        oldPlayer = tmp;
                        players.put(player.getUniqueId(), player);
                    }
                    for (Notification n : oldPlayer.getNotifications()) {
                        player.queueNote(n);
                    }
                }
            }
            else {
                ShopPlugin.logWarning("Could not load StoredPlayer of type " + o.getClass());
            }
        }
        return players.values();
    }
}
