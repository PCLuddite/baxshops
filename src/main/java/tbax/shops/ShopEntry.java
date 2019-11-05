package tbax.shops;

import org.bukkit.inventory.ItemStack;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.MathUtil;
import org.tbax.baxshops.items.ItemUtil;
import org.tbax.baxshops.serialization.states.State_00050;

import java.io.Serializable;
import java.util.HashMap;

public class ShopEntry implements Serializable
{
    private static final long serialVersionUID = 1L;
    public double retailPrice;
    public double refundPrice;
    public int quantity;
    public int itemID;
    public int itemDamage;
    public HashMap<Integer, Integer> enchantments;

    public ShopEntry() {
        this.retailPrice = -1.0;
        this.refundPrice = -1.0;
        this.enchantments = new HashMap<>();
    }

    public ItemStack toItemStack() {
        ItemStack stack = ItemUtil.fromItemId(itemID, (short)itemDamage);
        stack.setAmount(quantity);
        return stack;
    }

    public BaxEntry modernize(State_00050 state00050)
    {
        BaxEntry baxEntry = new BaxEntry();
        baxEntry.setRefundPrice(MathUtil.roundedDouble(refundPrice));
        baxEntry.setRetailPrice(MathUtil.roundedDouble(retailPrice));
        baxEntry.setAmount(quantity);
        baxEntry.setItem(ItemUtil.fromItemId(itemID, (short)itemDamage));
        return baxEntry;
    }
}
