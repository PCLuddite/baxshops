/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.StoredData;
import tbax.baxshops.serialization.states.State_30;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * A LollipopNotification notifies a player that someone sent him/her a
 * lollipop.
 */
public final class LollipopNotification implements UpgradeableNote, ConfigurationSerializable
{
    public static final double DEFAULT_TASTINESS = 40;
    private static final Map<Double, String> adjectives = new LinkedHashMap<>();
    static {
        adjectives.put(0.0, "a disgusting");
        adjectives.put(10.0, "a bad");
        adjectives.put(20.0, "an icky");
        adjectives.put(30.0, "a bland");
        adjectives.put(40.0, "a");
        adjectives.put(50.0, "an OK");
        adjectives.put(55.0, "a better-than-average");
        adjectives.put(60.0, "a good");
        adjectives.put(70.0, "a great");
        adjectives.put(80.0, "a tasty");
        adjectives.put(90.0, "a delicious");
        adjectives.put(99.0, "a wonderful");
    }

    private UUID sender;
    private double tastiness;

    public LollipopNotification(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        if (StoredData.getLoadedState() == State_30.VERSION) {
            deserialize30(map);
        }
        else {
            deserialize(map);
        }
    }

    public LollipopNotification(OfflinePlayer sender, double tastiness)
    {
        this.sender = sender.getUniqueId();
        this.tastiness = tastiness < 0 ? 0 : tastiness > 100 ? 100 : tastiness;
    }

    @Override
    public void deserialize30(@NotNull SafeMap map)
    {
        sender = State_30.getPlayerId(map.getString("sender"));
        tastiness = map.getDouble("tastiness");
    }

    @Override
    public void deserialize(@NotNull SafeMap map)
    {
        sender = map.getUUID("sender");
        tastiness = map.getDouble("tastiness");
    }

    public OfflinePlayer getSender()
    {
        return ShopPlugin.getOfflinePlayer(sender);
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        return getMessage();
    }

    @Override
    public @NotNull String getMessage()
    {
        return getSender().getName() + " sent you a " + getTastiness() + " lollipop";
    }

    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("sender", getSender().getUniqueId().toString());
        args.put("tastiness", tastiness);
        return args;
    }

    public static LollipopNotification deserialize(Map<String, Object> args)
    {
        return new LollipopNotification(args);
    }

    public static LollipopNotification valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    public String getTastiness()
    {
        String adjective = null;
        for (Entry<Double, String> entry : adjectives.entrySet()) {
            if (tastiness >= entry.getKey()) {
                adjective = entry.getValue();
            }
        }
        return adjective;
    }
}