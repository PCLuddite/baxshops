package tbax.baxshops.serialization;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerMap implements Map<UUID, StoredPlayer>
{
    private Map<UUID, StoredPlayer> players = new HashMap<>();
    private Map<String, UUID> playerNames = new HashMap<>();
    private Map<UUID, UUID> survivorship = new HashMap<>();

    @Override
    public int size()
    {
        return players.size();
    }

    @Override
    public boolean isEmpty()
    {
        return players.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        if (key instanceof UUID)
            return containsKey((UUID)key);
        if (key instanceof String)
            return containsKey((String)key);
        throw new ClassCastException();
    }

    public boolean containsKey(UUID playerId)
    {
        return players.containsKey(playerId);
    }

    public boolean containsKey(String playerName)
    {
        return playerNames.containsKey(playerName);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return players.containsValue(value);
    }

    @Override
    public StoredPlayer get(Object key)
    {
        if (key instanceof UUID)
            return get((UUID)key);
        if (key instanceof String)
            return get((String)key);
        throw new ClassCastException();
    }

    public StoredPlayer get(UUID playerId)
    {
        return players.get(survivorId(playerId));
    }

    public StoredPlayer get(String playerName)
    {
        UUID id = playerNames.get(playerName);
        if (id == null) {
            StoredPlayer player = new StoredPlayer(playerName);
            players.put(player.getUniqueId(), player);
            playerNames.put(player.getName(), player.getUniqueId());
            return player;
        }
        else {
            return players.get(id);
        }
    }

    @Override
    public StoredPlayer put(UUID key, StoredPlayer value)
    {
        StoredPlayer player = players.put(key, value);
        if (player != null)
            playerNames.remove(player.getName());
        playerNames.put(value.getName(), value.getUniqueId());
        return player;
    }

    @Override
    public StoredPlayer remove(Object key)
    {
        if (key instanceof UUID)
            return remove((UUID)key);
        if (key instanceof String)
            return remove((String)key);
        return null;
    }

    public StoredPlayer remove(UUID playerId)
    {
        StoredPlayer player = players.remove(playerId);
        if (player != null)
            playerNames.remove(player.getName());
        return player;
    }

    public StoredPlayer remove(String playerName)
    {
        UUID uuid = playerNames.remove(playerName);
        if (uuid != null)
            return players.remove(uuid);
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends UUID, ? extends StoredPlayer> m)
    {
        players.putAll(m);
        playerNames.clear();
        for(StoredPlayer value : players.values()) {
            playerNames.put(value.getName(), value.getUniqueId());
        }
    }

    @Override
    public void clear()
    {
        players.clear();
        playerNames.clear();
    }

    @NotNull
    @Override
    public Set<UUID> keySet()
    {
        return players.keySet();
    }

    @NotNull
    @Override
    public Collection<StoredPlayer> values()
    {
        return players.values();
    }

    @NotNull
    @Override
    public Set<Entry<UUID, StoredPlayer>> entrySet()
    {
        return players.entrySet();
    }

    public StoredPlayer convertLegacy(Player player)
    {
        UUID id = playerNames.get(player.getName());
        if (id == null || !players.get(id).isLegacyPlayer())
            return null;
        StoredPlayer storedPlayer = remove(id);
        storedPlayer.convertLegacy(player);
        put(storedPlayer);
        survivorship.put(id, storedPlayer.getUniqueId());
        return storedPlayer;
    }

    @SuppressWarnings("UnusedReturnValue")
    private StoredPlayer put(StoredPlayer storedPlayer)
    {
        return put(storedPlayer.getUniqueId(), storedPlayer);
    }

    public UUID survivorId(UUID playerId)
    {
        UUID id = survivorship.get(playerId);
        if (id == null)
            return playerId;
        return id;
    }
}
