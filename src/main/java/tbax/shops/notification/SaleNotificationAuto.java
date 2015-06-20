/* 
 * The MIT License
 *
 * Copyright © 2015 Timothy Baxendale (pcluddite@hotmail.com) and 
 * Copyright © 2012 Nathan Dinsmore and Sam Lazarus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package tbax.shops.notification;

import org.bukkit.entity.Player;

import tbax.shops.Main;
import tbax.shops.Resources;
import tbax.shops.Shop;
import tbax.shops.ShopEntry;

/**
 * A SaleNotification notifies a player that his/her sale of an
 * item was successful.
 */
public class SaleNotificationAuto implements Claimable {
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
	 * The seller of the item
	 */
	public String seller;
        
	/**
	 * Constructs a new notification.
	 * @param shop the shop to which the seller was selling
	 * @param entry an entry for the item (note: not the one in the shop)
	 * @param seller the seller of the item
	 */
	public SaleNotificationAuto(Shop shop, ShopEntry entry, String seller) {
            this.shop = shop;
            this.entry = entry;
            this.seller = seller;
	}
        
        public boolean claim(Player player) {
            if (Resources.giveToPlayer(player, entry.toItemStack())) {
                if (entry.quantity == 1) {
                    player.sendMessage("§fThe items have been added to your inventory.");
                }
                else {
                    player.sendMessage("§fThe item has been added to your inventory.");
                }
                return true;
            }
            else {
                return false;
            }
        }
	
	@Override
	public String getMessage(Player player) {
            return getMessage(player == null ? null : player.getName(), shop, entry, seller);
            
	}
        
        public static String getMessage(String buyer, Shop shop, ShopEntry entry, String seller) {
            if (buyer == null || !buyer.equals(shop.owner)) {
                return String.format("§1%s §fsold §1%s §e%d %s§F for §a$%.2f§F",
                            seller, shop.owner, entry.quantity, Main.instance.res.getItemName(entry),
                            entry.refundPrice * entry.quantity);
            }
            else {
                return String.format("§1%s §fsold you §e%d %s§f for §a$%.2f§F",
                            seller, entry.quantity, Main.instance.res.getItemName(entry),
                            entry.refundPrice * entry.quantity);
            }
        }
}
