/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

/**
 * A ShopSelection represents a user's selected shop.
 * Shops are selected by right- or left-clicking on them in-game.
 */
public final class ShopSelection
{
    /**
     * The selected shop
     */
    private BaxShop shop;

    /**
     * The displayed inventory page number
     */
    private int page = 0;

    /**
     * Whether the player who selected this shop owns it 
     */
    private boolean owner;

    private Location location;

    public ShopSelection()
    {
    }

    public ShopSelection(Location shopLoc, Player p, BaxShop selected)
    {
        owner = shop.getOwner().equals(p);
        location = shopLoc;
        shop = selected;
    }

    public void setLocation(Location value)
    {
        location = value;
    }

    public Location getLocation()
    {
        return location;
    }

    public boolean isOwner()
    {
        return owner;
    }

    public void setIsOwner(boolean value)
    {
        owner = value;
    }

    public void setShop(BaxShop value)
    {
        shop = value;
    }

    public BaxShop getShop()
    {
        return shop;
    }

    public void setPage(int value)
    {
        page = value;
    }

    public int getPage()
    {
        return page;
    }
        
    public void showListing(CommandSender sender)
    {
        int pages = shop.getPages();
        if (pages == 0) {
            sender.sendMessage(Format.header("Empty"));
            sender.sendMessage("");
            sender.sendMessage("This shop has no items");
            int stop = BaxShop.ITEMS_PER_PAGE - 2;
            if (owner) {
                sender.sendMessage("Use /shop add to add items");
                stop--;
            }
            for (int i = 0; i < stop; ++i) {
                sender.sendMessage("");
            }
            return;
        }
        sender.sendMessage(Format.header(String.format("Showing page %d of %d", page + 1, pages)));
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

    public String[] getSignText()
    {
        return shop.getSignText(location);
    }

    public String getSignTextString()
    {
        return shop.getSignTextString(location);
    }

    public ItemStack toItem()
    {
        return shop.toItem(location);
    }
}
