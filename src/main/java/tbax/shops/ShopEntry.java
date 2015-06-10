package tbax.shops;

import tbax.baxshops.Main;
import java.io.Serializable;
import java.util.HashMap;
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
    public double retailPrice = -1;
    /**
     * The price per unit to sell this item to the store
     */
    public double refundPrice = -1;
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
    public HashMap<Integer, Integer> enchantments = new HashMap<>();

    /**
     * Sets the item associated with this shop entry.
     * @param item an ItemStack
     */
    public void setItem(ItemStack item) {
        this.quantity = item.getAmount();
        this.itemID = item.getTypeId();
        this.itemDamage = item.getDurability();
        this.extractEnchantments(item);
    }

    protected void extractEnchantments(ItemStack item) {
        for (Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            enchantments.put(entry.getKey().getId(), entry.getValue());
        }
    }

    /**
     * Sets the amount of the item stack associated with this entry.
     * @param amount the quantity of the item
     */
    public void setAmount(int amount) {
        this.quantity = amount;
    }

    /**
     * Converts this entry to an item stack.
     * @return an item stack
     */
    public ItemStack toItemStack() {
        ItemStack i = new ItemStack(itemID, quantity, (short) itemDamage);
        for (Entry<Integer, Integer> entry : enchantments.entrySet()) {
            i.addUnsafeEnchantment(Enchantment.getById(entry.getKey()), entry.getValue());
        }
        return i;
    }

    public String toString(int index) {
        return null;
    }
}
