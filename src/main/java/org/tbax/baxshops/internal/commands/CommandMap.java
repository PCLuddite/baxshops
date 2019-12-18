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
package org.tbax.baxshops.internal.commands;

import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.commands.BaxShopCommand;
import org.tbax.baxshops.commands.ShopCmdActor;

import java.util.*;
import java.util.stream.Collectors;

public final class CommandMap implements Map<String, BaxShopCommand>
{
    private final Map<String, BaxShopCommand> cmds = new HashMap<>();
    private final Set<String> names = new HashSet<>();

    public CommandMap()
    {
    }

    public CommandMap(@NotNull Collection<Class<? extends BaxShopCommand>> cmdClasses) throws InstantiationException, IllegalAccessException
    {
        for (Class<? extends BaxShopCommand> command : cmdClasses) {
            add(command);
        }
    }

    @Override
    public int size()
    {
        return cmds.size();
    }

    @Override
    public boolean isEmpty()
    {
        return cmds.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return cmds.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return cmds.containsValue(value);
    }

    @Override
    public BaxShopCommand get(Object key)
    {
        return cmds.get(key);
    }

    @Override
    public BaxShopCommand put(String key, BaxShopCommand value)
    {
        names.add(value.getName());
        return cmds.put(key, value);
    }

    @Override
    public BaxShopCommand remove(Object key)
    {
        return cmds.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends BaxShopCommand> m)
    {
        cmds.putAll(m);
    }

    @Override
    public void clear()
    {
        cmds.clear();
        names.clear();
    }

    @Override
    public Set<String> keySet()
    {
        return cmds.keySet();
    }

    @Override
    public Collection<BaxShopCommand> values()
    {
        return cmds.values();
    }

    @Override
    public Set<Entry<String, BaxShopCommand>> entrySet()
    {
        return cmds.entrySet();
    }

    public CommandMap getOwnerCommands(ShopCmdActor actor)
    {
        return cmds.entrySet().stream()
            .filter(entry -> entry.getValue().requiresOwner(actor))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, CommandMap::new));
    }

    public CommandMap getAdminCommands(ShopCmdActor actor)
    {
        return cmds.entrySet().stream()
            .filter(entry -> entry.getValue().requiresAdmin())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, CommandMap::new));
    }

    public CommandMap getSelectionCommands(ShopCmdActor actor) {
        return cmds.entrySet().stream()
            .filter(entry -> entry.getValue().requiresSelection(actor))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, CommandMap::new));
    }

    public void add(@NotNull Class<? extends BaxShopCommand> cmdClass) throws InstantiationException, IllegalAccessException
    {
        BaxShopCommand cmd = cmdClass.newInstance();
        put(cmd.getName(), cmd);
        for (String alias : cmd.getAliases()) {
            put(alias, cmd);
        }
    }

    public Set<String> getNames()
    {
        return names;
    }
}
