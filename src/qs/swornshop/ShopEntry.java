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
	public float retailPrice = -1;
	/**
	 * The price per unit to sell this item to the store
	 */
	public float refundPrice = -1;
	/**
	 * The item stack associated with this shop entry
	 */
	public ItemStack item;
	/**
	 * The item's ID
	 */
	public int itemID;
	/**
	 * The item's damage value (durability)
	 */
	public int itemDamage;
	
	/**
	 * Sets the item associated with this shop entry.
	 */
	public void setItem(ItemStack item) {
		this.item = item;
		this.itemID = item.getTypeId();
		this.itemDamage = item.getDurability();
	}
	
	public String toString() {
		int quantity = item.getAmount();
		return refundPrice < 0 ?
			String.format(quantity == 0 ?
				"§C§M(%d) %s ($%.2f)" :
				"§7(%d) §F%s §B($%.2f)",
				quantity, Main.instance.getItemName(this), retailPrice) :
			String.format(quantity == 0 ?
				"§C§M(%d) %s ($%.2f) ($%.2f)" :
				"§7(%d) §F%s §B($%.2f) §7($%.2f)",
				quantity, Main.instance.getItemName(this), retailPrice, refundPrice);
	}

	/**
	 * Sets the amount of the item stack associated with this entry.
	 * @param amount the quantity of the item
	 */
	public void setAmount(int amount) {
		if (item.getAmount() == 0)
			item = new ItemStack(itemID, amount, (short) itemDamage);
		else
			item.setAmount(amount);
	}
}
