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
package tbax.baxshops.serialization;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.Resources;
import tbax.baxshops.serialization.annotations.SerializedAs;

import java.util.Map;
import java.util.UUID;

public class StoredPlayer implements OfflinePlayer, UpgradeableSerializable
{
    public static final UUID DUMMY_UUID = UUID.fromString("326a36ea-b465-3192-a4f7-c313f347edc9");
    public static final String DUMMY_NAME = "world";
    public static final StoredPlayer DUMMY = new StoredPlayer(DUMMY_NAME, DUMMY_UUID);

    public static final UUID ERROR_UUID = UUID.fromString("3d748006-ddc3-4f1b-a7c9-01fab68d0797");
    public static final String ERROR_NAME = Resources.ERROR_INLINE;
    public static final StoredPlayer ERROR = new StoredPlayer(ERROR_NAME, ERROR_UUID);

    private UUID uuid;

    @SerializedAs("legacy")
    private boolean legacyPlayer;

    @SerializedAs("name")
    private String lastSeenName;

    public StoredPlayer(String name, UUID uuid)
    {
        this.uuid = uuid;
        this.lastSeenName = name;
    }

    public StoredPlayer(Player player)
    {
        legacyPlayer = false;
        uuid = player.getUniqueId();
        lastSeenName = player.getName();
    }

    public StoredPlayer(String name)
    {
        legacyPlayer = true;
        uuid = UUID.randomUUID();
        lastSeenName = name;
    }

    @SuppressWarnings("unused")
    public StoredPlayer(Map<String, Object> args)
    {
        UpgradeableSerialization.upgrade(this, args);
    }

    @Override
    public void upgrade00400(@NotNull SafeMap map)
    {
        uuid = map.getUUID("uuid", UUID.randomUUID());
        lastSeenName = map.getString("name", uuid.toString());
        legacyPlayer = map.getBoolean("legacy", true);
    }

    public OfflinePlayer getOfflinePlayer()
    {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.isOnline()) {
            lastSeenName = player.getPlayer().getName();
        }
        return  player;
    }

    public boolean isLegacyPlayer()
    {
        return legacyPlayer;
    }

    public void convertLegacy(Player player)
    {
        this.legacyPlayer = false;
        uuid = player.getUniqueId();
        lastSeenName = player.getName();
    }

    @Override
    public boolean isOnline()
    {
        return getOfflinePlayer().isOnline();
    }

    @Override
    public String getName()
    {
        if (getOfflinePlayer().isOnline()) {
            return lastSeenName = getOfflinePlayer().getPlayer().getName();
        }
        else {
            return lastSeenName;
        }
    }

    @Override
    public UUID getUniqueId()
    {
        return uuid;
    }

    @Override
    public boolean isBanned()
    {
        return getOfflinePlayer().isBanned();
    }

    @Override
    public boolean isWhitelisted()
    {
        return getOfflinePlayer().isWhitelisted();
    }

    @Override
    public void setWhitelisted(boolean value)
    {
        getOfflinePlayer().setWhitelisted(value);
    }

    @Override
    public Player getPlayer()
    {
        return getOfflinePlayer().getPlayer();
    }

    @Override
    public long getFirstPlayed()
    {
        return getOfflinePlayer().getFirstPlayed();
    }

    @Override
    public long getLastPlayed()
    {
        return getOfflinePlayer().getLastPlayed();
    }

    @Override
    public boolean hasPlayedBefore()
    {
        return getOfflinePlayer().hasPlayedBefore();
    }

    @Override
    public Location getBedSpawnLocation()
    {
        return getOfflinePlayer().getBedSpawnLocation();
    }

    @Override
    public boolean isOp()
    {
        return getOfflinePlayer().isOp();
    }

    @Override
    public void setOp(boolean value)
    {
        getOfflinePlayer().setOp(value);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj instanceof OfflinePlayer)
            return equals((OfflinePlayer)obj);
        return false;
    }

    public boolean equals(OfflinePlayer player)
    {
        if (player == null)
            return false;
        return uuid.equals(player.getUniqueId());
    }

    @Override
    public String toString()
    {
        return getName();
    }

    public static StoredPlayer deserialize(Map<String, Object> args)
    {
        return new StoredPlayer(args);
    }

    public static StoredPlayer valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
