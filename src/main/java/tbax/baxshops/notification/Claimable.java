/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import org.bukkit.inventory.ItemStack;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.Resources;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.CommandWarningException;
import tbax.baxshops.errors.PrematureAbortException;

/**
 * A Claimable represents a notification which must wait for
 * certain conditions to be true before completing an action.
 * When a Claimable notification is sent, the notification is
 * automatically claimed if the user is online.
 */
public abstract class Claimable implements Notification
{
    /**
     * An entry for the offered item
     */
    protected BaxEntry entry;
    
    /**
     * Attempts to claim this notification.
     * @param actor the actor who is claiming the notification
     * @return true if the notification could be claimed, false otherwise
     */
    public boolean claim(ShopCmdActor actor)
    {
        ItemStack stack = entry.toItemStack();
        try {
            actor.giveItem(stack);
            actor.sendMessage(Resources.ITEM_ADDED);
            return true;
        }
        catch (CommandErrorException e) {
            actor.sendError(e.getMessage());
            return false;
        }
        catch (CommandWarningException e) {
            actor.sendWarning(e.getMessage());
            return false;
        }
        catch (PrematureAbortException e) {
            actor.sendMessage(e.getMessage());
            return true;
        }
    }
}
