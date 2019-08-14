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
package tbax.baxshops.serialization.states;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxShop;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.notification.NoteSet;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.*;

@Deprecated
public class State_00411 extends LegacyState
{
    public static final double VERSION = 4.11;
    private ShopPlugin plugin;

    public State_00411(ShopPlugin plugin)
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
        List<StoredPlayer> players = new ArrayList<>();
        if (!state.isList("players")) {
            return players;
        }
        for(Object o : state.getList("players")) {
            if (o instanceof StoredPlayer) {
                players.add((StoredPlayer)o);
            }
            else {
                plugin.getLogger().warning("Could not load StoredPlayer of type " + o.getClass());
            }
        }
        return players;
    }

    @Override
    public @NotNull Collection<NoteSet> buildNotifications(@NotNull FileConfiguration state)
    {
        List<NoteSet> notes = new ArrayList<>();
        if (!state.isConfigurationSection("notes")) {
            return notes;
        }

        NoteSet errorNotes = new NoteSet(StoredPlayer.ERROR_UUID);

        for (Map.Entry entry : state.getConfigurationSection("notes").getValues(false).entrySet()) {
            UUID playerId;
            try {
                playerId = UUID.fromString(entry.getKey().toString());
            }
            catch (IllegalArgumentException e) {
                playerId = StoredPlayer.ERROR_UUID;
                ShopPlugin.logWarning("UUID " + entry.getKey() + " is invalid. Notes will be assigned to an error user.");
            }
            if (entry.getValue() instanceof List) {
                Deque<Notification> pending = new ArrayDeque<>(((List) entry.getValue()).size());
                for (Object o : (List) entry.getValue()) {
                    if (o instanceof Notification) {
                        pending.add((Notification) o);
                    }
                    else {
                        ShopPlugin.logWarning("Could not load Notification of type " + entry.getValue().getClass());
                    }
                }
                if (playerId.equals(StoredPlayer.ERROR_UUID)) {
                    errorNotes.getNotifications().addAll(pending);
                }
                else {
                    notes.add(new NoteSet(playerId, pending));
                }
            }
            else {
                ShopPlugin.logWarning("Could not load notification list for " + entry.getKey());
            }
        }

        if (!errorNotes.getNotifications().isEmpty()) {
            notes.add(errorNotes);
        }

        return notes;
    }

    @Override
    public @NotNull ShopPlugin getPlugin()
    {
        return plugin;
    }
}
