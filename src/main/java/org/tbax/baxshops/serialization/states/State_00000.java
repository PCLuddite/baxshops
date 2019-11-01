package org.tbax.baxshops.serialization.states;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.ShopPlugin;
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
