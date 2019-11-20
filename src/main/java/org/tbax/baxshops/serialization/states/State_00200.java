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
package org.tbax.baxshops.serialization.states;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.items.ItemUtil;
import org.tbax.baxshops.notification.Notification;
import org.tbax.baxshops.serialization.PlayerMap;
import org.tbax.baxshops.serialization.SavedState;
import org.tbax.baxshops.serialization.StateLoader;
import org.tbax.baxshops.serialization.StoredPlayer;
import tbax.shops.serialization.JsonState;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class State_00200 implements StateLoader
{
    private ShopPlugin plugin;
    private PlayerMap players = new PlayerMap();
    private Map<Integer, BaxShop> legacyShops = new HashMap<>();
    private Map<Integer, String> ownerNames = new HashMap<>();
    private JsonState jsonState;
    private JsonObject rootObject;

    public State_00200(ShopPlugin plugin)
    {
        this.plugin = plugin;
        jsonState = new JsonState(plugin);
    }

    public static File getJsonFile(JavaPlugin plugin)
    {
        return new File(plugin.getDataFolder(), JsonState.JSON_FILE_PATH);
    }

    public static double getJsonFileVersion(JavaPlugin plugin)
    {
        File stateLocation = getJsonFile(plugin);
        try (JsonReader jr = new JsonReader(new FileReader(stateLocation))) {
            JsonParser p = new JsonParser();
            JsonElement element = p.parse(jr);
            if (element.isJsonObject()) {
                return element.getAsJsonObject().get("version").getAsDouble();
            }
        }
        catch (Exception e) {
            // do nothing
        }
        return 0d;
    }

    @Override
    public SavedState loadState(@NotNull FileConfiguration state)
    {
        rootObject = jsonState.loadState();
        if (rootObject == null) {
            ShopPlugin.logSevere("Unable to load old shops.json! A new state file will be created.");
            return new SavedState(plugin);
        }
        else {
            try {
                ItemUtil.loadLegacyItems(plugin);
                ItemUtil.loadLegacyEnchants();
            }
            catch (IOException e) {
                ShopPlugin.logSevere("Unable to load legacy items list required for conversion!");
            }
            return StateLoader.super.loadState(state);
        }
    }

    @Override
    public @NotNull Collection<BaxShop> buildShops(@NotNull FileConfiguration state)
    {
        jsonState.loadShops(this, rootObject.get("shops").getAsJsonObject());
        ShopPlugin.logInfo("Converting shops data...");
        for (Map.Entry<Integer, tbax.shops.BaxShop> entry : jsonState.shops.entrySet()) {
            legacyShops.put(entry.getKey(), entry.getValue().modernize(this));
            ownerNames.put(entry.getKey(), entry.getValue().owner);
        }
        return legacyShops.values();
    }

    @Override
    public @NotNull Collection<StoredPlayer> buildPlayers(@NotNull FileConfiguration state)
    {
        ShopPlugin.logInfo("Loading notification data...");
        jsonState.loadNotes(this, rootObject.get("notes").getAsJsonObject());
        ShopPlugin.logInfo("Converting notification data...");
        for (Map.Entry<String, ArrayDeque<tbax.shops.notification.Notification>> entry : jsonState.pending.entrySet()) {
            StoredPlayer player = registerPlayer(entry.getKey());
            while (!entry.getValue().isEmpty()) {
                Notification newNote = entry.getValue().removeFirst().getNewNote(this);
                newNote.setSentDate(null);
                player.queueNote(newNote);
            }
        }
        return players.values();
    }

    @Override
    public @NotNull ShopPlugin getPlugin()
    {
        return plugin;
    }

    public StoredPlayer registerPlayer(String playerName)
    {
        if (playerName == null)
            return StoredPlayer.ERROR;
        return players.getOrCreate(playerName).get(0);
    }

    public BaxShop getShop(int shopId)
    {
        BaxShop shop = legacyShops.get(shopId);
        if (shop == null) {
            ShopPlugin.logWarning("Legacy shop " + shopId + " does not exist! A dummy shop will be used instead.");
            legacyShops.put(shopId, shop = BaxShop.DUMMY_SHOP);
        }
        return shop;
    }

    public String getShopOwner(int shopId)
    {
        String owner = ownerNames.get(shopId);
        if (owner == null) {
            BaxShop shop = getShop(shopId);
            if (shop == BaxShop.DUMMY_SHOP) {
                owner = StoredPlayer.DUMMY_NAME;
            }
            else {
                ShopPlugin.logWarning("Legacy shop " + shopId + " does not have an owner! This will be assigned to a dummy user.");
                shop.setOwner(StoredPlayer.DUMMY);
                ownerNames.put(shopId, owner = StoredPlayer.DUMMY_NAME);
            }
        }
        return owner;
    }
}
