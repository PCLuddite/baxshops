/*
 * Copyright © 2012 Nathan Dinsmore and Sam Lazarus
 * Modifications Copyright © Timothy Baxendale
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
        baxEntry.canBuy(true);
        if (refundPrice >= 0) baxEntry.canSell(true);
        baxEntry.setAmount(quantity);
        baxEntry.setItem(ItemUtil.fromItemId(itemID, (short)itemDamage));
        return baxEntry;
    }
}
