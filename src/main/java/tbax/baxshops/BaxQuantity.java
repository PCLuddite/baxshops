/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.PrematureAbortException;

public class BaxQuantity
{
    private String argument;
    private BaxEntry entry;
    private Player player;

    public BaxQuantity(Player pl, BaxEntry baxEntry, String arg)
    {
        argument = arg;
        entry = baxEntry;
        player = pl;
    }

    public int getQuantity() throws PrematureAbortException
    {
        try {
            return Integer.parseInt(argument);
        }
        catch (NumberFormatException e) {
            if (isAll()) {
                return getAmountInInventory();
            }
            else if (isMost()) {
                return player.getInventory().getItemInMainHand().getAmount() - 1;
            }
            throw new CommandErrorException(e, argument + " is not a valid quantity");
        }
    }

    private int getAmountInInventory()
    {
        int qty = 0;
        for(ItemStack stack : player.getInventory()) {
            if (entry.isSimilar(stack)) {
                qty += stack.getAmount();
            }
        }
        return qty;
    }

    public boolean isAll()
    {
        return "all".equalsIgnoreCase(argument);
    }

    public boolean isMost()
    {
        return "most".equalsIgnoreCase(argument);
    }
}
