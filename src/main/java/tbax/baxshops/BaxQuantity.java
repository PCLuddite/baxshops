/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.PrematureAbortException;

import java.util.Arrays;

public class BaxQuantity
{
    private String argument;
    private ItemStack stack;
    private Iterable<ItemStack> inventory;

    public BaxQuantity(@NotNull String arg, ItemStack item, @NotNull Iterable<ItemStack> inv)
    {
        argument = arg;
        stack = item;
        inventory = inv;
    }

    public BaxQuantity(@NotNull String arg, ItemStack item, @NotNull ItemStack[] inv)
    {
        this(arg, item, Arrays.asList(inv));
    }

    public void setItem(ItemStack stack)
    {
        this.stack = stack;
    }

    public ItemStack getItem()
    {
        return stack;
    }

    public void setInventory(@NotNull Iterable<ItemStack> inv)
    {
        inventory = inv;
    }

    public void setInventory(@NotNull ItemStack[] inv)
    {
        setInventory(Arrays.asList(inv));
    }

    public @NotNull Iterable<ItemStack> getInventory()
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
            throw new CommandErrorException(e, "'" + argument + "' is not a valid quantity");
        }
    }

    public boolean isQuantityNotAny()
    {
        return isQuantityNotAny(argument);
    }

    public boolean isQuantity()
    {
        return isQuantity(argument);
    }

    public static boolean isQuantityNotAny(String qty)
    {
        try {
            Integer.parseInt(qty);
            return true;
        }
        catch (NumberFormatException e) {
            return "all".equalsIgnoreCase(qty) || "most".equalsIgnoreCase(qty);
        }
    }

    public static boolean isQuantity(String qty)
    {
        return isQuantityNotAny(qty) || "any".equalsIgnoreCase(qty);
    }

    private int getAmountInInventory()
    {
        int qty = 0;
        for(ItemStack stack : inventory) {
            if (stack != null && stack.isSimilar(this.stack)) {
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
