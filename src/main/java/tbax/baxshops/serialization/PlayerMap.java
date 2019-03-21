/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerMap implements Map<UUID, StoredPlayer>
{
    private Map<UUID, StoredPlayer> players = new HashMap<>();
    private Map<String, List<UUID>> playerNames = new HashMap<>();
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
        if (key instanceof String) {
            List<StoredPlayer> uuids = get((String) key);
            if (uuids == null || uuids.isEmpty())
                return null;
            if (uuids.size() > 1)
                throw new ClassCastException();
            return uuids.get(0);
        }
        throw new ClassCastException();
    }

    public StoredPlayer get(UUID playerId)
    {
        return players.get(survivorId(playerId));
    }

    public List<StoredPlayer> get(String playerName)
    {
        List<UUID> uuids = playerNames.get(playerName);
        if (uuids == null) {
            StoredPlayer player = new StoredPlayer(playerName);
            uuids = new ArrayList<>();
            uuids.add(player.getUniqueId());
            players.put(player.getUniqueId(), player);
            playerNames.put(player.getName(), uuids);
        }
        StoredPlayer[] ret = new StoredPlayer[uuids.size()];
        for(int x = 0; x < ret.length; ++x) {
            ret[x] = get(uuids.get(x));
        }
        return Arrays.asList(ret);
    }

    @Override
    public StoredPlayer put(UUID key, StoredPlayer value)
    {
        StoredPlayer player = players.put(key, value);
        List<UUID> uuids;
        if (player == null) {
            uuids = new ArrayList<>();
        }
        else {
            uuids = playerNames.remove(player.getName());
        }
        uuids.add(value.getUniqueId());
        playerNames.put(value.getName(), uuids);
        return player;
    }

    @Override
    public StoredPlayer remove(Object key)
    {
        if (key instanceof UUID)
            return remove((UUID)key);
        if (key instanceof String) {
            List<StoredPlayer> ret = remove((String) key);
            if (ret == null || ret.isEmpty())
                return null;
            return ret.get(0);
        }
        return null;
    }

    public StoredPlayer remove(UUID playerId)
    {
        StoredPlayer player = players.remove(playerId);
        if (player != null)
            playerNames.remove(player.getName());
        return player;
    }

    public List<StoredPlayer> remove(String playerName)
    {
        List<UUID> uuids = playerNames.remove(playerName);
        if (uuids == null)
            return null;
        StoredPlayer[] ret = new StoredPlayer[uuids.size()];
        for(int x = 0; x < uuids.size(); ++x) {
            ret[x] = remove(get(x));
        }
        return Arrays.asList(ret);
    }

    @Override
    public void putAll(@NotNull Map<? extends UUID, ? extends StoredPlayer> m)
    {
        players.putAll(m);
        playerNames.clear();
        for(StoredPlayer value : players.values()) {
            List<UUID> uuids = playerNames.remove(value.getName());
            if (uuids == null)
                uuids = new ArrayList<>();
            uuids.add(value.getUniqueId());
            playerNames.put(value.getName(), uuids);
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
        List<UUID> uuids = playerNames.get(player.getName());
        if (uuids == null || uuids.isEmpty())
            return null;

        for (UUID id : uuids) {
            StoredPlayer storedPlayer = get(id);
            if (storedPlayer != null && storedPlayer.isLegacyPlayer()) {
                players.remove(id);
                storedPlayer.convertLegacy(player);
                put(storedPlayer);
                survivorship.put(id, storedPlayer.getUniqueId());
                return storedPlayer;
            }
        }        
        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    public StoredPlayer put(StoredPlayer storedPlayer)
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
