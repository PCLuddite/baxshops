/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.Format;
import tbax.baxshops.serialization.ItemNames;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public class DeletedShopClaim extends Claimable implements ConfigurationSerializable
{
    public String owner;
    
    public DeletedShopClaim(Map<String, Object> args)
    {
        entry = (BaxEntry)args.get("entry");
        owner = (String)args.get("owner");
    }
    
    public DeletedShopClaim(String owner, BaxEntry entry)
    {
        this.owner = owner;
        this.entry = entry;
    }
    
    @Override
    public String getMessage(Player player)
    {
        return String.format("The shop that had this entry no longer exists. You have %s outstanding.", Format.itemname(entry.getAmount(), ItemNames.getName(entry)));
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
    public boolean checkIntegrity()
    {
        return true;
    }
    
}