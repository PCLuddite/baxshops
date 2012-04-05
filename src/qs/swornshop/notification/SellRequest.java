package qs.swornshop.notification;

import java.util.Calendar;
import java.util.Date;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.entity.Player;

import qs.swornshop.Main;
import qs.swornshop.Shop;
import qs.swornshop.ShopEntry;

/**
 * A SellRequest notifies a shop owner that someone has requested
 * to sell him/her an item.
 * SellRequests expire after five days.
 */
public class SellRequest implements Request, TimedNotification {
	
	/**
	 * An entry for the offered item
	 */
	public ShopEntry entry;
	/**
	 * The shop to which the item is being sold
	 */
	public Shop shop;
	/**
	 * The date at which the request expires
	 */
	public long expirationDate;
	/**
	 * The seller of the item
	 */
	public String seller;
	
	/**
	 * Constructs a new notification.
	 * @param shop the shop to which the seller was selling
	 * @param entry an entry for the item (note: not the one in the shop)
	 * @param seller the seller of the item
	 */
	public SellRequest(Shop shop, ShopEntry entry, String seller) {
		this.shop = shop;
		this.entry = entry;
		this.seller = seller;
		
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, 5);
		this.expirationDate = c.getTimeInMillis();
	}
	
	@Override
	public String getMessage(Player player) {
		return player == null || !player.getName().equals(shop.owner) ?
			String.format("%s wants to sell %s §B%d %s§F for §B$%.2f§F",
					seller, shop.owner, entry.item.getAmount(), Main.instance.getItemName(entry),
					entry.refundPrice * entry.item.getAmount()) :
			String.format("%s wants to sell you §B%d %s§F for §B%.2f§F",
					seller, entry.item.getAmount(), Main.instance.getItemName(entry),
					entry.refundPrice * entry.item.getAmount());
	}
	
	@Override
	public boolean accept(Player player) {
		if (!Main.inventoryFitsItem(player, entry.item)){
			Main.sendError(player, "Your inventory is full");
			return false;
		}
		Economy econ = Main.econ;
		float price = entry.item.getAmount() * entry.refundPrice;
		if (!econ.has(shop.owner, price)) {
			Main.sendError(player, "You do not have sufficient funds to accept this offer");
			return false;
		}
		econ.withdrawPlayer(shop.owner, price);
		econ.depositPlayer(seller, price);
		
		player.getInventory().addItem(entry.item);
		
		SaleNotification n = new SaleNotification(shop, entry, seller);
		Main.instance.sendNotification(seller, n);
		
		player.sendMessage("§BOffer accepted");
		return true;
	}
	
	@Override
	public boolean reject(Player player) {
		SaleRejection n = new SaleRejection(shop, entry, seller);
		Main.instance.sendNotification(seller, n);
		player.sendMessage("§COffer rejected");
		return true;
	}

	@Override
	public long expirationDate() {
		return expirationDate;
	}
	
}
