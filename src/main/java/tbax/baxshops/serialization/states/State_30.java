/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization.states;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxShop;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.notification.*;
import tbax.baxshops.serialization.StateConversion;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.*;

public class State_30 implements StateLoader
{

    private ShopPlugin plugin;

    public State_30(@NotNull ShopPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public StoredData load(@NotNull FileConfiguration config)
    {
        StateConversion.clearMaps();
        return StateLoader.super.load(config);
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
                BaxShop shop = (BaxShop) o;
                StateConversion.addLegacyShop(shop.getLegacyId(), shop.getId());
            } else {
                plugin.getLogger().warning("Could not load BaxShop of type " + o.getClass());
            }
        }
        return shops;
    }

    @Override
    public @NotNull Collection<StoredPlayer> buildPlayerList(@NotNull FileConfiguration config)
    {
        return StateConversion.getPlayers();
    }

    @Override
    public @NotNull Collection<NoteSet> buildNotificationList(@NotNull FileConfiguration config)
    {
        List<NoteSet> noteSets = new ArrayList<>();
        if (!config.isConfigurationSection("notes")) {
            return noteSets;
        }
        for (Map.Entry entry : config.getConfigurationSection("notes").getValues(false).entrySet()) {
            OfflinePlayer player = StateConversion.getPlayer(entry.getKey().toString());
            if (!(entry.getValue() instanceof List)) {
                plugin.getLogger().warning("Could not load notifications of type " + entry.getValue().getClass());
            }
            else {
                Deque<Notification> pending = new ArrayDeque<>(((List) entry.getValue()).size());
                for (Object o : (List) entry.getValue()) {
                    if (o instanceof Notification) {
                        pending.add((Notification) o);
                    }
                    else if (o instanceof DeprecatedNote) {
                        pending.add(((DeprecatedNote) o).getNewNote());
                    }
                    else {
                        plugin.getLogger().warning("Could not load Notification of type " + entry.getValue().getClass());
                    }
                }
                if (StoredPlayer.DUMMY.equals(player)) {
                    Deque<Notification> errors = new ArrayDeque<>();
                    while (!pending.isEmpty()) {
                        Notification n = pending.remove();
                        if (n instanceof Claimable || n instanceof Request) {
                            errors.add(n);
                        }
                    }
                    if (!errors.isEmpty()) {
                        plugin.getLogger().warning("There is one or more claim or request notification assigned to the dummy player. " +
                            "These cannot be honored and will be assigned to an error user. The configuration file will need to be fixed manually.");
                        noteSets.add(new NoteSet(StoredPlayer.ERROR_UUID, errors));
                    }
                }
                else {
                    noteSets.add(new NoteSet(player.getUniqueId(), pending));
                }
            }
        }
        return noteSets;
    }

    @Override
    public @NotNull ShopPlugin getPlugin()
    {
        return plugin;
    }

}
