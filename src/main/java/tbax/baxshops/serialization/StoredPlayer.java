/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StoredPlayer implements OfflinePlayer
{
    public static final UUID DUMMY_UUID = UUID.fromString("326a36ea-b465-3192-a4f7-c313f347edc9");
    public static final String DUMMY_NAME = "world";
    public static final StoredPlayer DUMMY = new StoredPlayer(DUMMY_NAME, DUMMY_UUID);

    private UUID uuid;
    private String lastSeenName;
    private boolean legacyPlayer;

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
        SafeMap map = new SafeMap(args);
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
            return getOfflinePlayer().getPlayer().getName();
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
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("uuid", uuid.toString());
        args.put("name", lastSeenName);
        args.put("legacy", legacyPlayer);
        return args;
    }

    @Override
    public String toString()
    {
        return getName();
    }
}
