package qs.shops;

import org.bukkit.inventory.ItemStack;
import org.tbax.baxshops.items.ItemUtil;

import java.io.Serializable;
import java.util.HashMap;

/**
 * A ShopEntry represents a single item in the inventory of a shop,
 * with a retail (buy) price and a refund (sell) price. If the item's
 * refund price is -1, the item cannot be sold to the shop.
 */
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

	public ItemStack toItemStack()
	{
		ItemStack stack = ItemUtil.fromItemId(itemID, (short)itemDamage);
		stack.setAmount(quantity);
		return stack;
	}
}
