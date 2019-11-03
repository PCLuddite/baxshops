package org.tbax.baxshops.serialization.states;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.items.ItemUtil;
import org.tbax.baxshops.notification.Claimable;
import org.tbax.baxshops.notification.Notification;
import org.tbax.baxshops.notification.Request;
import org.tbax.baxshops.serialization.SavedState;
import org.tbax.baxshops.serialization.StateLoader;
import org.tbax.baxshops.serialization.StoredPlayer;
import qs.shops.Shop;
import qs.shops.serialization.State;

import java.io.*;
import java.util.*;

public class State_00000 implements StateLoader
{
    public static final double VERSION = 0;
    private ShopPlugin plugin;
    private State nathanState;
    private Map<Shop, BaxShop> shopMap = new HashMap<>();
    private Map<String, StoredPlayer> playerMap = new HashMap<>();

    public State_00000(ShopPlugin plugin)
    {
        this.plugin = plugin;
        playerMap.put(StoredPlayer.DUMMY_NAME, StoredPlayer.DUMMY);
        playerMap.put(StoredPlayer.ERROR_NAME, StoredPlayer.ERROR);
    }

    public static File getNathanFile(JavaPlugin plugin)
    {
        return new File(plugin.getDataFolder(), "shops.dat");
    }

    @Override
    public SavedState loadState(@NotNull FileConfiguration state)
    {
        File stateLocation = getNathanFile(getPlugin());
        try {
            try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(stateLocation))) {
                nathanState = (State)stream.readObject();
            }
            ItemUtil.loadLegacyItems(plugin);
        }
        catch (ClassCastException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
            ShopPlugin.logSevere("Unable to load shops.dat! A new state will be loaded");
            return new SavedState(getPlugin());
        }
        return StateLoader.super.loadState(state);
    }

    @Override
    public @NotNull Collection<BaxShop> buildShops(@NotNull FileConfiguration state)
    {
        for (Map.Entry<Location, qs.shops.Shop> entry : nathanState.getShops().entrySet()) {
            registerShop(entry.getValue());
        }
        return shopMap.values();
    }

    @Override
    public @NotNull Collection<StoredPlayer> buildPlayers(@NotNull FileConfiguration state)
    {
        for (Map.Entry<String, ArrayDeque<qs.shops.notification.Notification>> entry : nathanState.pending.entrySet()) {
            StoredPlayer player = registerPlayer(entry.getKey());
            for (qs.shops.notification.Notification note : entry.getValue()) {
                player.queueNote(note.getNewNote(this));
            }

            if (StoredPlayer.DUMMY.equals(player)) {
                Deque<Notification> errors = new ArrayDeque<>();
                while (player.getNotificationCount() > 0) {
                    Notification n = player.dequeueNote();
                    if (n instanceof Claimable || n instanceof Request) {
                        errors.add(n);
                    }
                }
                if (!errors.isEmpty()) {
                    plugin.getLogger().warning("There is one or more claim or request notification assigned to the dummy player. " +
                            "These cannot be honored and will be assigned to an error user. The configuration file will need to be fixed manually.");
                    do {
                        StoredPlayer.ERROR.queueNote(errors.removeFirst());
                    }
                    while(!errors.isEmpty());
                }
            }
        }
        return playerMap.values();
    }

    @Override
    public @NotNull ShopPlugin getPlugin()
    {
        return plugin;
    }

    public UUID registerShop(Shop shop)
    {
        BaxShop baxShop = shopMap.get(shop);
        if (baxShop == null) {
            baxShop = BaxShop.fromNathan(shop, this);
            shopMap.put(shop, baxShop);
        }
        return baxShop.getId();
    }

    public StoredPlayer registerPlayer(String name)
    {
        StoredPlayer player = playerMap.get(name);
        if (player == null) {
            player = new StoredPlayer(name);
            playerMap.put(name, player);
        }
        return player;
    }
}
