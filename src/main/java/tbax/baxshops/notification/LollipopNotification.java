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
import tbax.baxshops.Format;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.SavedState;
import tbax.baxshops.serialization.StoredPlayer;
import tbax.baxshops.serialization.states.State_30;
import tbax.baxshops.serialization.states.State_40;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A LollipopNotification notifies a player that someone sent him/her a
 * lollipop.
 */
public final class LollipopNotification implements UpgradeableNote, ConfigurationSerializable
{
    public static final double DEFAULT_TASTINESS = 40;
    private static final String[] adjectives =  {
        "a disgusting",
        "a bad",
        "an icky",
        "a bland",
        "a",
        "an OK",
        "a good",
        "a great",
        "a tasty",
        "a delicious",
        "a wonderful"
    };

    private UUID sender;
    private UUID recipient;
    private double tastiness;
    private Date date;

    public LollipopNotification(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        if (SavedState.getLoadedState() == State_40.VERSION) {
            deserialize40(map);
        }
        if (SavedState.getLoadedState() == State_30.VERSION) {
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
        this.date = new Date();
    }

    @Override
    public void deserialize30(@NotNull SafeMap map)
    {
        sender = State_30.getPlayerId(map.getString("sender"));
        recipient = StoredPlayer.ERROR_UUID;
        tastiness = map.getDouble("tastiness");
    }

    @Override
    public void deserialize40(@NotNull SafeMap map)
    {
        sender = map.getUUID("sender");
        tastiness = map.getDouble("tastiness");
    }

    @Override
    public void deserialize(@NotNull SafeMap map)
    {
        sender = map.getUUID("sender");
        recipient = map.getUUID("receiver");
        tastiness = map.getDouble("tastiness");
        date = map.getDate("date");
    }

    public OfflinePlayer getSender()
    {
        return ShopPlugin.getOfflinePlayer(sender);
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getRecipient().equals(sender)) {
            return getSender().getName() + " sent you " + getTastiness() + " lollipop";
        }
        else {
            return getMessage();
        }
    }

    private OfflinePlayer getRecipient()
    {
        return ShopPlugin.getOfflinePlayer(recipient);
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s sent %s %s lollipop", getSender().getName(), getRecipient().getName(), getTastiness());
    }

    @Override
    public Date getSentDate()
    {
        return date;
    }

    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("sender", getSender().getUniqueId().toString());
        args.put("recipient", getRecipient().getUniqueId().toString());
        args.put("tastiness", tastiness);
        args.put("date", date == null ? null : Format.date(date));
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
        if (tastiness >= 55.0 && tastiness < 60.0)
            return "a better-than-average";
        String adjective = "a";
        for (int i = 0; i < adjectives.length; ++i) {
            if (tastiness >= (i * 100)) {
                adjective = adjectives[i];
            }
        }
        return adjective;
    }
}
