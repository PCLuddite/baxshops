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

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.internal.items.ItemUtil;
import org.tbax.baxshops.notification.Claimable;
import org.tbax.baxshops.notification.Notification;
import org.tbax.baxshops.notification.Request;
import org.tbax.baxshops.internal.serialization.StateLoader;
import org.tbax.baxshops.serialization.StoredPlayer;
import tbax.shops.Shop;
import tbax.shops.serialization.State2;

import java.io.*;
import java.util.*;

public class StateLoader_00100 implements StateLoader
{
    public static final double VERSION = 1.0;
    private ShopPlugin plugin;
    private State2 state2;
    private Map<tbax.shops.Shop, BaxShop> shopMap = new HashMap<>();
    private Map<String, StoredPlayer> playerMap = new HashMap<>();

    public StateLoader_00100(ShopPlugin plugin)
    {
        this.plugin = plugin;
        playerMap.put(StoredPlayer.DUMMY_NAME, StoredPlayer.DUMMY);
        playerMap.put(StoredPlayer.ERROR_NAME, StoredPlayer.ERROR);
    }

    public static File getState2File(JavaPlugin plugin)
    {
        return new File(plugin.getDataFolder(), "shops.dat");
    }

    @Override
    public FileConfiguration readFile(@NotNull File stateLocation)
    {
        try {
            try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(stateLocation))) {
                state2 = (State2)stream.readObject();
            }
            ItemUtil.loadLegacyItems(plugin);
            ItemUtil.loadLegacyEnchants();
        }
        catch (ClassCastException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
            ShopPlugin.logSevere("Unable to load shops.dat! A new state will be loaded");
            return null;
        }
        return new YamlConfiguration();
    }

    public static Class<?> getSerializedClass(JavaPlugin plugin) throws IOException
    {
        try {
            try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(getState2File(plugin)))) {
                return stream.readObject().getClass();
            }
        }
        catch (ClassNotFoundException e) {
            ShopPlugin.logSevere("Unknown class in shops.dat!");
            return null;
        }
    }

    @Override
    public @NotNull Collection<BaxShop> buildShops(@NotNull FileConfiguration state)
    {
        for (Map.Entry<Location, tbax.shops.BaxShop> entry : state2.getShops().entrySet()) {
            registerShop(entry.getValue());
        }
        return shopMap.values();
    }

    @Override
    public @NotNull Collection<StoredPlayer> buildPlayers(@NotNull FileConfiguration state)
    {
        for (Map.Entry<String, ArrayDeque<tbax.shops.notification.Notification>> entry : state2.pending.entrySet()) {
            StoredPlayer player = registerPlayer(entry.getKey());
            for (tbax.shops.notification.Notification note : entry.getValue()) {
                Notification newNote = note.getNewNote(this);
                newNote.setSentDate(null);
                player.queueNote(newNote);
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

    public BaxShop registerShop(Shop shop)
    {
        BaxShop baxShop = shopMap.get(shop);
        if (baxShop == null) {
            baxShop = shop.modernize(this);
            shopMap.put(shop, baxShop);
        }
        return baxShop;
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
