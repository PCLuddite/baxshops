package qs.swornshop;

import java.util.Date;

import org.bukkit.inventory.ItemStack;

public class PendingEntry {
	
	public int price;
	
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
	
	public String shopOwner;
	
	public Shop shop;
	
	public Date expirationDate;
	
	public float sellprice;
	
	public String seller;
	
	public Boolean toOwner;
	
	public Boolean accepted;
	
	public PendingEntry(String owner, Shop shop, float sellprice, String seller, Boolean toOwner){
		this.shopOwner = owner;
		this.shop = shop;
		this.sellprice = sellprice;
		this.seller = seller;
		this.toOwner = toOwner;
		Date date = new Date();
		long time = date.getTime();
		time = time + 432000000;
		this.expirationDate = new Date(time);
		
	}
	public PendingEntry(String owner, Shop shop, float sellprice, Boolean toOwner, Boolean accepted){
		this.shopOwner = owner;
		this.shop = shop;
		this.sellprice = sellprice;
		this.toOwner = toOwner;
		this.accepted = accepted;
		Date date = new Date();
		long time = date.getTime();
		time = time + 432000000;
		this.expirationDate = new Date(time);
		
	}
	
	public void setItem(ItemStack item) {
		this.item = item;
		this.itemID = item.getTypeId();
		this.itemDamage = item.getDurability();
	}
	
	public String toString(Main p) {
		return null;
	}
	
	//TODO FINISH THIS
	
}
