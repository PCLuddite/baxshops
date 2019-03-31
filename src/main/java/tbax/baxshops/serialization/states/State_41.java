/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization.states;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxShop;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.notification.NoteSet;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.serialization.StateLoader;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.*;

public class State_41 implements StateLoader
{
    public static final double VERSION = 4.1;
    private ShopPlugin plugin;

    public State_41(ShopPlugin plugin)
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
                plugin.getLogger().warning("Could not readFromDisk BaxShop of type " + o.getClass());
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
                plugin.getLogger().warning("Could not readFromDisk StoredPlayer of type " + o.getClass());
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
                        ShopPlugin.logWarning("Could not readFromDisk Notification of type " + entry.getValue().getClass());
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
                ShopPlugin.logWarning("Could not readFromDisk notification list for " + entry.getKey());
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
