package qs.swornshop;

/**
 * A ShopSelection represents a user's selected shop.
 * Shops are selected by right- or left-clicking on them in-game.
 */
public class ShopSelection {
	public ShopSelection() { }
	
	/**
	 * The selected shop
	 */
	public Shop shop;
	/**
	 * The displayed inventory page number
	 */
	public int page = 0;
	/**
	 * Whether the player who selected this shop owns it 
	 */
	public boolean isOwner;
}
