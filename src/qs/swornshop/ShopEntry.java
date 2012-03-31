package qs.swornshop;

import org.bukkit.inventory.ItemStack;

/**
 * A ShopEntry represents a single item in the inventory of a shop,
 * with a retail (buy) price and a refund (sell) price. If the item's
 * refund price is -1, the item cannot be sold to the shop.
 */
public class ShopEntry {
	/**
	 * The price per unit to buy this item from the store
	 */
	public float retailPrice;
	/**
	 * The price per unit to sell this item to the store
	 */
	public float refundPrice;
	/**
	 * The item stack associated with this shop entry
	 */
	public ItemStack item;
	
	public String toString(Main p) {
		int quantity = item.getAmount();
		return refundPrice < 0 ?
			String.format(quantity == 0 ?
				"§C(%d) %s ($%.2f)" :
				"§7(%d) §F%s §B($%.2f)",
				quantity, p.getItemName(item), retailPrice) :
			String.format(quantity == 0 ?
				"§C(%d) %s ($%.2f) ($%.2f)" :
				"§7(%d) §F%s §B($%.2f) §7($%.2f)",
				quantity, p.getItemName(item), retailPrice, refundPrice);
	}
}
