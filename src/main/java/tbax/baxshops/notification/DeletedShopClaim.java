/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.Format;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.SavedState;
import tbax.baxshops.serialization.states.State_30;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class DeletedShopClaim implements UpgradeableNote, Claimable
{
    private UUID owner;
    private BaxEntry entry;
    private Date date;

    public DeletedShopClaim(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        if (SavedState.getLoadedState() == State_30.VERSION) {
            deserialize30(map);
        }
        else {
            deserialize(map);
        }
    }

    public DeletedShopClaim(OfflinePlayer owner, BaxEntry entry)
    {
        this.owner = owner.getUniqueId();
        this.entry = new BaxEntry(entry);
        this.date = new Date();
    }

    public DeletedShopClaim(UUID owner, BaxEntry entry)
    {
        this(ShopPlugin.getOfflinePlayer(owner), entry);
    }

    @Override
    public void deserialize30(@NotNull SafeMap map)
    {
        entry = map.getBaxEntry("entry");
        owner = State_30.getPlayerId(map.getString("owner"));
    }

    @Override
    public void deserialize(@NotNull SafeMap map)
    {
        entry = map.getBaxEntry("entry");
        owner = map.getUUID("owner");
        date = map.getDate("date");
    }

    public OfflinePlayer getOwner()
    {
        return ShopPlugin.getOfflinePlayer(owner);
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getOwner().equals(sender)) {
            return String.format("The shop that had this entry no longer exists. You have %s outstanding.", entry.getFormattedName());
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("The shop that had this entry no longer exists. %s has %s outstanding.",
            Format.username(owner), entry.getFormattedName()
        );
    }

    @Override
    public Date getSentDate()
    {
        return date;
    }

    @Override
    public Map<String, Object> serialize()
    {
        HashMap<String, Object> args = new HashMap<>();
        args.put("entry", entry);
        args.put("owner", getOwner().getUniqueId().toString());
        args.put("date", date == null ? null : Format.date(date));
        return args;
    }

    public static DeletedShopClaim deserialize(Map<String, Object> args)
    {
        return new DeletedShopClaim(args);
    }

    public static DeletedShopClaim valueOf(Map<String, Object> args)
    {
        return new DeletedShopClaim(args);
    }

    @Override
    public BaxEntry getEntry()
    {
        return entry;
    }
}
