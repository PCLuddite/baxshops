/* 
 * The MIT License
 *
 * Copyright © 2013-2015 Timothy Baxendale (pcluddite@hotmail.com) and 
 * Copyright © 2012 Nathan Dinsmore and Sam Lazarus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package tbax.baxshops.notification;

import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.Main;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public class SaleNotificationAutoClaim extends SaleNotificationAuto implements ConfigurationSerializable, Claimable 
{
    public SaleNotificationAutoClaim(Map<String, Object> args)
    {
        super(args);
    }

    /**
     * Constructs a new notification.
     * @param buyer the buyer of the item
     * @param entry an entry for the item (note: not the one in the shop)
     * @param seller the seller of the item
     */
    public SaleNotificationAutoClaim(String buyer, String seller, BaxEntry entry) 
    {
        super(buyer, seller, entry);
    }
    
    @Override
    public Map<String, Object> serialize()
    {
        return super.serialize();
    }

    @Override
    public boolean claim(Player player) 
    {
        if (Main.tryGiveItem(player, entry.toItemStack())) {
            if (entry.getAmount() == 1) {
                player.sendMessage("The item have been added to your inventory.");
            }
            else {
                player.sendMessage("The items has been added to your inventory.");
            }
            return true;
        }
        else {
            return false;
        }
    }
    
    public static SaleNotificationAuto deserialize(Map<String, Object> args)
    {
        return new SaleNotificationAuto(args);
    }
    
    public static SaleNotificationAuto valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
