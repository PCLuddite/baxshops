package qs.swornshop;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

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
	 * The item stack associated with this shop entry
	 */
	public transient ItemStack item;
	
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
	public HashMap<Integer, Integer> enchantments;
	
	/**
	 * Sets the item associated with this shop entry.
	 */
	public void setItem(ItemStack item) {
		this.item = item;
		this.itemID = item.getTypeId();
		this.itemDamage = item.getDurability();
	}
	
	public String toString(int index) {
		int quantity = item.getAmount();
		String name = Main.instance.getItemName(this);
		Map<Enchantment, Integer> enchantments = item.getEnchantments();
		if (enchantments.size() > 0) {
			name = "§D" + name + " (";
			for (Entry<Enchantment, Integer> e : enchantments.entrySet()) {
				name += e.getKey().getName().substring(0, 3) + e.getValue().toString() + ", ";
			}
			name = name.substring(0, name.length() - 2) + ")";
		}
		return refundPrice < 0 ?
			(quantity == -8 ? 
				String.format(
					"%d§7. (§E99§7) §F%s §B($%.2f)",
					index, name, retailPrice) :
				String.format(quantity == 0 ?
					"§C§M%d. (%d) %s ($%.2f)" :
					"%d§7. (%d) §F%s §B($%.2f)",
					index, quantity, name, retailPrice)) :
			(quantity == -8 ? 
				String.format(
					"%d§7. (§E99§7) §F%s §B($%.2f) §7($%.2f)",
					index, name, retailPrice, refundPrice) :
				String.format(quantity == 0 ?
					"§C§M%d. (%d) %s ($%.2f) ($%.2f)" :
					"%d.§7 (%d) §F%s §B($%.2f) §7($%.2f)",
					index, quantity, name, retailPrice, refundPrice));
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
