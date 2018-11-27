/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.Format;
import tbax.baxshops.commands.ShopCmdActor;

import java.util.HashMap;
import java.util.Map;

public class DeletedShopClaim implements Claimable
{
    private OfflinePlayer owner;
    private BaxEntry entry;

    public DeletedShopClaim(Map<String, Object> args)
    {
        entry = (BaxEntry)args.get("entry");
        owner = (OfflinePlayer)args.get("owner");
    }

    public DeletedShopClaim(OfflinePlayer owner, BaxEntry entry)
    {
        this.owner = owner;
        this.entry = entry;
    }

    @Override
    public String getMessage(CommandSender sender)
    {
        if (sender == null || !sender.equals(owner)) {
            return String.format("The shop that had this entry no longer exists. %s has %s outstanding.",
                Format.username(owner.getName()), entry.getFormattedName()
            );
        }
        else {
            return String.format("The shop that had this entry no longer exists. You have %s outstanding.", entry.getFormattedName());
        }
    }

    @Override
    public Map<String, Object> serialize()
    {
        HashMap<String, Object> args = new HashMap<>();
        args.put("entry", entry);
        args.put("owner", owner);
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

    @Override
    public boolean claim(ShopCmdActor actor)
    {
        return actor.tryGiveItem(entry.toItemStack());
    }
}