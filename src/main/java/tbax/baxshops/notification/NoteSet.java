/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

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
        recipient = UUID.fromString((String)args.get("recipient"));
        notes.addAll((List)args.get("notes"));
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
        args.put("recipient", recipient.toString());
        args.put("notes", new ArrayList<>(notes));
        return args;
    }
}
