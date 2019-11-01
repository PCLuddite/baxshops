package qs.shops.notification;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.serialization.qs.NathanConverter;
import qs.shops.Main;
import qs.shops.Shop;
import qs.shops.ShopEntry;

import java.util.Calendar;
import java.util.Date;

/**
 * A SellRequest notifies a shop owner that someone has requested
 * to sell him/her an item.
 * SellRequests expire after five days.
 */
public class SellRequest implements Request, TimedNotification {
	private static final long serialVersionUID = 1L;
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
		return null;
	}
	
	@Override
	public boolean accept(Player player) {
		ItemStack item = entry.toItemStack();
		if (!Main.inventoryFitsItem(player, item)){
			Main.sendError(player, "Your inventory is full");
			return false;
		}
		Economy econ = Main.econ;
		float price = entry.quantity * entry.refundPrice;
		if (!econ.has(shop.owner, price)) {
			Main.sendError(player, "You do not have sufficient funds to accept this offer");
			return false;
		}
		econ.withdrawPlayer(shop.owner, price);
		econ.depositPlayer(seller, price);
		
		player.getInventory().addItem(item);
		
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

	// begin modified class

	@Override
	public @NotNull Class<? extends org.tbax.baxshops.notification.Notification> getNewNoteClass()
	{
		return org.tbax.baxshops.notification.SaleRejection.class;
	}

	@Override
	public @NotNull org.tbax.baxshops.notification.Notification getNewNote()
	{
		return new org.tbax.baxshops.notification.SaleRejection(
				NathanConverter.registerShop(shop),
				NathanConverter.registerPlayer(shop.owner),
				NathanConverter.registerPlayer(seller),
				BaxEntry.fromNathan(entry)
		);
	}
}
