/* 
 * The MIT License
 *
 * Copyright © 2013-2015 Timothy Baxendale (pcluddite@hotmail.com) and 
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
