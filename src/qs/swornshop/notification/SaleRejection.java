package qs.swornshop.notification;

import org.bukkit.entity.Player;

import qs.swornshop.Main;
import qs.swornshop.Shop;
import qs.swornshop.ShopEntry;

public class SaleRejection implements Claimable {
	
	/**
	 * An entry for the offered item
	 */
	public ShopEntry entry;
	/**
	 * The shop to which the item is being sold
	 */
	public Shop shop;
	/**
	 * The seller of the item
	 */
	public String seller;
	
	public SaleRejection(Shop shop, ShopEntry entry, String seller) {
		this.shop = shop;
		this.entry = entry;
		this.seller = seller;
	}
	
	@Override
	public String getMessage(Player player) {
		return player == null || !player.getName().equals(seller) ?
			String.format("%s rejected %s's request to sell §B%d %s§F for §B$%.2f§F",
					shop.owner, seller, entry.item.getAmount(), Main.instance.getItemName(entry),
					entry.refundPrice * entry.item.getAmount()) :
			String.format("%s rejected your request to sell §B%d %s§F for §B%.2f§F",
					shop.owner, entry.item.getAmount(), Main.instance.getItemName(entry),
					entry.refundPrice * entry.item.getAmount());
	}

	@Override
	public boolean claim(Player player) {
		if (!Main.inventoryFitsItem(player, entry.item)){
			Main.sendError(player, "Your inventory is full");
			return false;
		}
		player.getInventory().addItem(entry.item);
		return true;
	}

}
