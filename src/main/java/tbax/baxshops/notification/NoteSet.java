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
package tbax.baxshops.notification;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.UpgradeableSerialization;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.SerializationException;
import tbax.baxshops.serialization.UpgradeableSerializable;

import java.util.*;

@Deprecated
public final class NoteSet implements UpgradeableSerializable
{
    private UUID recipient;
    private Deque<Notification> notes = new ArrayDeque<>();

    public NoteSet(UUID recipient)
    {
        this.recipient = recipient;
    }

    public NoteSet(UUID recipient, Collection<? extends Notification> notes)
    {
        this.recipient = recipient;
        this.notes.addAll(notes);
    }

    public NoteSet(Map<String, Object> args)
    {
        UpgradeableSerialization.upgrade(this, args);
    }

    @Override
    public void upgrade00400(@NotNull SafeMap map)
    {
        recipient = map.getUUID("recipient");
        notes.addAll(map.getList("notes"));
    }

    @Override
    public void upgrade00410(@NotNull SafeMap map)
    {
        SerializationException.throwVersionException();
    }

    public void upgrade00421(@NotNull SafeMap map)
    {
        SerializationException.throwVersionException();
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
    public @NotNull Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("recipient", ShopPlugin.getOfflinePlayer(recipient).getUniqueId().toString());
        args.put("notes", new ArrayList<>(notes));
        return args;
    }
}
