/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StoredPlayer implements OfflinePlayer
{
    private UUID uuid;
    private String lastSeenName;

    public StoredPlayer(Player player)
    {
        uuid = player.getUniqueId();
        lastSeenName = player.getName();
    }

    public StoredPlayer(UUID uuid)
    {
        this.uuid = uuid;
    }

    public StoredPlayer(Map<String, Object> args)
    {
        uuid = UUID.fromString((String)args.get("uuid"));
        lastSeenName = (String)args.get("name");
    }

    public OfflinePlayer getOfflinePlayer()
    {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.isOnline()) {
            lastSeenName = player.getPlayer().getName();
        }
        return  player;
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
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("uuid", uuid.toString());
        args.put("name", lastSeenName);
        return args;
    }
}
