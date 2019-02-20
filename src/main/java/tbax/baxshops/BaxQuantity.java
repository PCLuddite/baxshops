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
    private ItemStack stack;
    private Iterable<ItemStack> inventory;

    public BaxQuantity(String arg)
    {
        this(arg, null, null);
    }

    public BaxQuantity(String arg, ItemStack item, Iterable<ItemStack> inv)
    {
        argument = arg;
        stack = item;
        inventory = inv;
    }

    public void setItem(ItemStack stack)
    {
        this.stack = stack;
    }

    public void getItem()
    {
        return stack;
    }

    public void setInventory(Iterable<ItemStack> inv)
    {
        inventory = inv;
    }

    public void getInventory()
    {
        return inventory;
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
                return stack.getAmount() - 1;
            }
            throw new CommandErrorException(e, argument + " is not a valid quantity");
        }
    }

    public boolean isQuantityNotAny()
    {
        try {
            getQuantity();
            return true;
        }
        catch(PrematureAbortException e) {
            return false;
        }
    }

    public boolean isQuantity()
    {
        return isQuantityNotAny() || isAny();
    }

    private int getAmountInInventory()
    {
        int qty = 0;
        for(ItemStack stack : inventory) {
            if (stack.isSimilar(this.stack)) {
                qty += stack.getAmount();
            }
        }
        return qty;
    }

    public boolean isAll()
    {
        return "all".equalsIgnoreCase(argument);
    }

    public boolean isAny()
    {
        return "any".equalsIgnoreCase(argument);
    }

    public boolean isMost()
    {
        return "most".equalsIgnoreCase(argument);
    }
}
