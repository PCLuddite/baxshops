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
package qs.shops;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.MathUtil;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.internal.items.ItemUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A ShopEntry represents a single item in the inventory of a shop,
 * with a retail (buy) price and a refund (sell) price. If the item's
 * refund price is -1, the item cannot be sold to the shop.
 */
@Deprecated
public class ShopEntry implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * The price per unit to buy this item from the store
	 */
	public float retailPrice = -1;
	/**
	 * The price per unit to sell this item to the store
	 */
	public float refundPrice = -1;
	/**
	 * The item's quantity
	 */
	public int quantity;
	/**
	 * The item's ID
	 */
	public int itemID;
	/**
	 * The item's damage value (durability)
	 */
	public int itemDamage;

	/**
	 * The item's enchantments
	 */
	public HashMap<Integer, Integer> enchantments = new HashMap<Integer, Integer>();

	public BaxEntry modernize()
	{
		BaxEntry baxEntry = new BaxEntry();
		baxEntry.setRefundPrice(MathUtil.roundedDouble(refundPrice));
		baxEntry.setRetailPrice(MathUtil.roundedDouble(retailPrice));
		baxEntry.canBuy(true);
		if (refundPrice >= 0) baxEntry.canSell(true);
		baxEntry.setAmount(quantity);
		baxEntry.setItem(toItemStack());
		return baxEntry;
	}

	public ItemStack toItemStack()
	{
		ItemStack stack = ItemUtil.fromItemId(itemID, (short)itemDamage);
		if (enchantments != null) {
			for (Map.Entry<Integer, Integer> entry : enchantments.entrySet()) {
				Enchantment e = ItemUtil.getLegacyEnchantment(entry.getKey());
				if (e == null) {
					ShopPlugin.logWarning("Unknown enchantment id " + entry.getKey() + ". This will not be added.");
				}
				else {
					stack.addUnsafeEnchantment(e, entry.getValue());
				}
			}
		}
		stack.setAmount(1);
		return stack;
	}
}
