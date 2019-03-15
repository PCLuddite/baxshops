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
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.StoredData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class DeletedShopClaim extends Claimable
{
    private UUID owner;
    private BaxEntry entry;

    public DeletedShopClaim(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        entry = map.getBaxEntry("entry");
        owner = map.getUUID("owner");
    }

    public DeletedShopClaim(OfflinePlayer owner, BaxEntry entry)
    {
        this.owner = owner.getUniqueId();
        this.entry = new BaxEntry(entry);
    }

    public DeletedShopClaim(UUID owner, BaxEntry entry)
    {
        this(StoredData.getOfflinePlayer(owner), entry);
    }

    public OfflinePlayer getOwner()
    {
        return StoredData.getOfflinePlayer(owner);
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
    public Map<String, Object> serialize()
    {
        HashMap<String, Object> args = new HashMap<>();
        args.put("entry", entry);
        args.put("owner", getOwner().getUniqueId().toString());
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
