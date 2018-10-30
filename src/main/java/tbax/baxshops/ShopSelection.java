/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops;

import tbax.baxshops.help.CommandHelp;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

/**
 * A ShopSelection represents a user's selected shop.
 * Shops are selected by right- or left-clicking on them in-game.
 */
public final class ShopSelection {
    public ShopSelection() { }

    /**
     * The selected shop
     */
    public BaxShop shop;
    /**
     * The displayed inventory page number
     */
    public int page = 0;
    /**
     * Whether the player who selected this shop owns it 
     */
    public boolean isOwner;

    public Location location;
        
    public void showListing(CommandSender sender) {
        int pages = shop.getPages();
        if (pages == 0) {
            sender.sendMessage(CommandHelp.header("Empty"));
            sender.sendMessage("");
            sender.sendMessage("This shop has no items");
            int stop = BaxShop.ITEMS_PER_PAGE - 2;
            if (isOwner) {
                sender.sendMessage("Use /shop add to add items");
                stop--;
            }
            for (int i = 0; i < stop; ++i) {
                sender.sendMessage("");
            }
            return;
        }
        sender.sendMessage(CommandHelp.header(String.format("Showing page %d of %d", page + 1, pages)));
        int i = page * BaxShop.ITEMS_PER_PAGE,
                stop = (page + 1) * BaxShop.ITEMS_PER_PAGE,
                max = Math.min(stop, shop.getInventorySize());
        for (; i < max; i++) {
            sender.sendMessage(shop.getEntryAt(i).toString(i + 1));
        }
        for (; i < stop; i++) {
            sender.sendMessage("");
        }
    }
}
