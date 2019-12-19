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
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.internal.notification.NoteSet;
import org.tbax.baxshops.internal.serialization.State;
import org.tbax.baxshops.serialization.StoredPlayer;

import java.util.*;

public class StateLoader_00400 extends LoaderWithNotes
{
    public static final double VERSION = 4.0;
    private ShopPlugin plugin;
    private Map<UUID, StoredPlayer> players = new HashMap<>();

    public StateLoader_00400(@NotNull ShopPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Collection<BaxShop> buildShops(@NotNull FileConfiguration state)
    {
        List<BaxShop> shops = new ArrayList<>();
        if (!state.isList("shops")) {
            return shops;
        }
        for (Object o : state.getList("shops")) {
            if (o instanceof BaxShop) {
                shops.add((BaxShop)o);
            }
            else {
                plugin.getLogger().warning("Could not load BaxShop of type " + o.getClass());
            }
        }
        return shops;
    }

    @Override
    public @NotNull Collection<StoredPlayer> buildPlayers(@NotNull FileConfiguration state)
    {
        if (!state.isList("players")) {
            return players.values();
        }
        for(Object o : state.getList("players")) {
            if (o instanceof StoredPlayer) {
                StoredPlayer player = (StoredPlayer)o;
                players.put(player.getUniqueId(), getPlayer(null, player.getUniqueId()));
            }
            else {
                plugin.getLogger().warning("Could not load StoredPlayer of type " + o.getClass());
            }
        }
        return players.values();
    }

    @Override
    public @NotNull Collection<NoteSet> buildNotifications(@NotNull FileConfiguration state)
    {
        List<NoteSet> notes = new ArrayList<>();
        if (!state.isList("notes")) {
            return notes;
        }
        for (Object o : state.getList("notes")) {
            if (o instanceof NoteSet) {
                notes.add((NoteSet)o);
            }
            else {
                plugin.getLogger().warning("Could not load NoteSet of type " + o.getClass());
            }
        }
        return notes;
    }

    @Override
    public @NotNull ShopPlugin getPlugin()
    {
        return plugin;
    }
}
