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
import tbax.baxshops.serialization.StoredPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class State_40 implements StateLoader
{
    private ShopPlugin plugin;

    public State_40(@NotNull ShopPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Collection<BaxShop> buildShopList(@NotNull FileConfiguration config)
    {
        List<BaxShop> shops = new ArrayList<>();
        if (!config.isList("shops")) {
            return shops;
        }
        for (Object o : config.getList("shops")) {
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
    public @NotNull Collection<StoredPlayer> buildPlayerList(@NotNull FileConfiguration config)
    {
        List<StoredPlayer> players = new ArrayList<>();
        if (!config.isList("players")) {
            return players;
        }
        for(Object o : config.getList("players")) {
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
    public @NotNull Collection<NoteSet> buildNotificationList(@NotNull FileConfiguration config)
    {
        List<NoteSet> notes = new ArrayList<>();
        if (!config.isList("notes")) {
            return notes;
        }
        for (Object o : config.getList("notes")) {
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
