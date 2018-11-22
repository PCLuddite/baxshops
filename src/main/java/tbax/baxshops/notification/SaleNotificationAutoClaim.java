/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.commands.ShopCmdActor;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public class SaleNotificationAutoClaim extends Claimable implements ConfigurationSerializable
{
    private SaleNotificationAuto note; // we're going to simulate multiple inheritance by holding this reference
    
    
    public SaleNotificationAutoClaim(Map<String, Object> args)
    {
        note = new SaleNotificationAuto(args);
    }

    /**
     * Constructs a new notification.
     * @param buyer the buyer of the item
     * @param entry an entry for the item (note: not the one in the shop)
     * @param seller the seller of the item
     */
    public SaleNotificationAutoClaim(String buyer, String seller, BaxEntry entry) 
    {
        note = new SaleNotificationAuto(buyer, seller, entry);
    }
    
    @Override
    public boolean claim(ShopCmdActor actor)
    {
        super.entry = note.entry;
        return super.claim(actor);
    }
    
    @Override
    public Map<String, Object> serialize()
    {
        return note.serialize();
    }
    
    public static SaleNotificationAuto deserialize(Map<String, Object> args)
    {
        return new SaleNotificationAuto(args);
    }
    
    public static SaleNotificationAuto valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    @Override
    public String getMessage(Player player)
    {
        return note.getMessage(player);
    }

    @Override
    public boolean checkIntegrity()
    {
        return true;
    }
}
