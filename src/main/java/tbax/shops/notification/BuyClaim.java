/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tbax.shops.notification;

import org.bukkit.entity.Player;
import tbax.shops.Main;
import tbax.shops.Resources;
import tbax.shops.Shop;
import tbax.shops.ShopEntry;

/**
 *
 * @author Timothy
 */
public class BuyClaim implements Claimable {
    private static final long serialVersionUID = 1L;
    /**
     * An entry for the purchased item
     */
    public ShopEntry entry;
    /**
     * The shop to which the item is being sold
     */
    public Shop shop;
    /**
     * The seller of the item
     */
    public String buyer;
    
    /**
     * Constructs a new notification.
     * @param shop the shop to which the seller was selling
     * @param entry an entry for the item (note: not the one in the shop)
     * @param buyer the buyer of the item
     */
    public BuyClaim(Shop shop, ShopEntry entry, String buyer) {
        this.shop = shop;
        this.entry = entry;
        this.buyer = buyer;
    }
    
    @Override
    public boolean claim(Player player) {
        return false;
    }
    
    public String getMessage(Player player) {
        return null;
    }
}
