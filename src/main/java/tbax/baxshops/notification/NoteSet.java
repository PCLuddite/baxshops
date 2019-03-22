/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.SafeMap;

import java.util.*;

@SuppressWarnings("WeakerAccess")
public final class NoteSet implements ConfigurationSerializable
{
    private UUID recipient;
    private Deque<Notification> notes = new ArrayDeque<>();

    public NoteSet(UUID recipient, Collection<? extends Notification> notes)
    {
        this.recipient = recipient;
        this.notes.addAll(notes);
    }

    public NoteSet(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        recipient = map.getUUID("recipient");
        notes.addAll(map.getList("notes"));
    }
    
    public UUID getRecipient()
    {
        return recipient;
    }
    
    public Deque<Notification> getNotifications()
    {
        return notes;
    }
    
    public static NoteSet deserialize(Map<String, Object> args)
    {
        return new NoteSet(args);
    }
    
    public static NoteSet valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("recipient", ShopPlugin.getOfflinePlayer(recipient).getUniqueId().toString());
        args.put("notes", new ArrayList<>(notes));
        return args;
    }
}
