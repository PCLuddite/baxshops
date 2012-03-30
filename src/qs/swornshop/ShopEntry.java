package qs.swornshop;

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
}
