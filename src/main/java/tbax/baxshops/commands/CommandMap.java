/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class CommandMap implements Map<String, BaxShopCommand>
{
    private final Map<String, BaxShopCommand> cmds = new HashMap<>();

    public CommandMap()
    {
    }

    public CommandMap(Class<? extends BaxShopCommand>... commands)
    {
        for(int x = 0; x < commands.length; ++x) {
            try {
                BaxShopCommand cmd = commands[x].newInstance();
                for(String alias : cmd.getAliases()) {
                    cmds.put(alias, cmd);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
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
        return cmds.put(key, value);
    }

    @Override
    public BaxShopCommand remove(Object key)
    {
        return cmds.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends BaxShopCommand> m)
    {
        cmds.putAll(m);
    }

    @Override
    public void clear()
    {
        cmds.clear();
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
}
