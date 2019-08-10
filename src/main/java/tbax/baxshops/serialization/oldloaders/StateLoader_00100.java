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
package tbax.baxshops.serialization.oldloaders;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxShop;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.notification.NoteSet;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.serialization.SavedState;
import tbax.baxshops.serialization.StateLoader;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.Collection;
import java.util.Deque;

@Deprecated
public abstract class StateLoader_00100 implements StateLoader
{
    @Override
    public SavedState loadState(@NotNull FileConfiguration state)
    {
        SavedState savedState = new SavedState(getPlugin());

        ShopPlugin.logInfo("Loading shop data...");
        Collection<BaxShop> shops = buildShops(state);
        ShopPlugin.logInfo("Loading notifications...");
        Collection<NoteSet> notes = buildNotifications(state);
        ShopPlugin.logInfo("Loading player data...");
        Collection<StoredPlayer> players = buildPlayers(state);

        for (StoredPlayer player : players) {
            addPlayer(savedState, player);
        }

        for (BaxShop shop : shops) {
            addShop(savedState, shop);
        }

        for (NoteSet noteSet : notes) {
            Deque<Notification> deque = noteSet.getNotifications();
            StoredPlayer player = savedState.getOfflinePlayer(noteSet.getRecipient());
            for (Notification note : deque) {
                player.queueNote(note);
            }
        }

        setConfig(savedState, loadConfig(getPlugin().getConfig()));
        return savedState;
    }
}
