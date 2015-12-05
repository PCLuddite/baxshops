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

import java.util.ArrayDeque;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import static tbax.baxshops.Main.econ;
import static tbax.baxshops.Main.log;
import static tbax.baxshops.Main.sendError;
import static tbax.baxshops.Main.sendWarning;
import tbax.baxshops.notification.Notification;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public class EventListener implements Listener
{
    private Main main;
    
    public EventListener(Main main)
    {
        this.main = main;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event)
    {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        loc.setY(loc.getY() + 1);
        if (Main.getState().getShop(loc) != null) {
            sendError(event.getPlayer(), "You cannot remove this block because there is a shop above it!");
            event.setCancelled(true); 
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Block b = event.getClickedBlock();
        if (b == null) {
            return;
        }
        
        BaxShop shop = Main.getState().getShop(b.getLocation());
        if (shop == null) {
            if (b.hasMetadata("shopid")) {
                shop = Main.getState().getShop(b.getMetadata("shopid").get(0).asLong());
                if (shop == null) {
                    event.getPlayer().sendMessage("This shop has been closed.");
                    return;
                }
            }
            else {
                return;
            }
        }
        Player pl = event.getPlayer();
        
        boolean isOwner = shop.owner.equals(pl.getName());
        
        /*if (!pl.hasPermission("shop.admin") && !isOwner) {
            if ((boolean)shop.getOption("whitelist_enabled") &&
                !((ArrayList<String>)shop.getOption("whitelist")).contains(pl.getName())) {
                sendError(pl, "You need to be whitelisted at this shop to view its inventory.");
                return;
            }
            if ((boolean)shop.getOption("blacklist_enabled") &&
                ((ArrayList<String>)shop.getOption("blacklist")).contains(pl.getName())) {
                sendError(pl, "You have been banned from this shop and cannot view its inventory.");
                return;
            }
        }*/
        
        ShopSelection selection = main.selectedShops.get(pl);
        if (selection == null) {
            selection = new ShopSelection();
            main.selectedShops.put(pl, selection);
        }
        selection.location = b.getLocation();
        
        //res.log.log(Level.INFO, "{0} selected {1}''s shop at ({2}, {3}, {4})", new Object[]{pl.getName(), shop.owner, selection.location.getBlockX(), selection.location.getBlockY(), selection.location.getBlockX()});
        
        if (selection.shop == shop) {
            int pages = shop.getPages();
            if (pages == 0) {
                selection.page = 0;
            }
            else {
                int delta = event.getAction() == Action.LEFT_CLICK_BLOCK ? 1 : -1;
                selection.page = (((selection.page + delta) % pages) + pages) % pages;
            }
            pl.sendMessage("");
            pl.sendMessage("");
        }
        else {
            selection.isOwner = isOwner;
            selection.shop = shop;
            selection.page = 0;
            StringBuilder intro = new StringBuilder(ChatColor.WHITE.toString());
            intro.append("Welcome to ");
            if (isOwner) {
                intro.append(Format.username("your"));
            }
            else {
                intro.append(Format.username(shop.owner)).append("'s");
            }
            intro.append(" shop\n");
            intro.append(ChatColor.GRAY.toString());
            intro.append("For help with shops, type /shop help.");
            pl.sendMessage(intro.toString());
        }

        selection.showListing(pl);

        event.setCancelled(true);
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            b.getState().update();
        }
    }
	
    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplosion(EntityExplodeEvent event)
    {
        for (Block b : event.blockList()) {
            Location loc = b.getLocation();
            if (Main.getState().getShop(loc) != null) {
                event.setCancelled(true);
                return;
            }
            loc.setY(loc.getY() + 1);
            if (Main.getState().getShop(loc) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        ItemStack item = event.getItemInHand();
        if (item.getType() == Material.SIGN && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasLore() && meta.getLore().get(0).startsWith("ID: ")) {
                long id = Long.parseLong(meta.getLore().get(0).substring(4));
                BaxShop shop = Main.getState().getShop(id);
                if (shop == null) {
                    sendWarning(event.getPlayer(), "This shop has been closed and can't be placed.");
                    event.setCancelled(true);
                }
                else {
                    Main.getState().addLocation(event.getPlayer(), event.getBlockPlaced().getLocation(), shop);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        ArrayDeque<Notification> p = Main.getState().getNotifications(event.getPlayer());
        if (!p.isEmpty()) {
            event.getPlayer().sendMessage(ChatColor.WHITE + "You have new notifications. Use " + Format.command("/shop notifications") + ChatColor.WHITE + " to view them");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Player pl = event.getPlayer();
        ShopSelection s = main.selectedShops.get(pl);
        if (s != null) {
            Location shopLoc = s.location;
            Location pLoc = event.getTo();
            if (shopLoc.getWorld() != pl.getWorld() || shopLoc.distanceSquared(pLoc) > Resources.SHOP_RANGE) {
                if (s.isOwner) {
                    pl.sendMessage("[Left " + Format.username("your") + " shop]");
                }
                else {
                    pl.sendMessage("[Left " + Format.username(s.shop.owner) + "'s shop]");
                }
                main.selectedShops.remove(event.getPlayer());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        String name = main.getConfig().getString("DeathTax.GoesTo", null);
        if (name != null) {
            Player pl = event.getEntity();
            if (econ.has(pl.getName(), 100.00) && isStupidDeath(event.getDeathMessage())) {
                double death_tax = econ.getBalance(pl.getName()) * main.getConfig().getDouble("DeathTax.Percentage", 0.04);
                econ.withdrawPlayer(pl.getName(), death_tax);
                econ.depositPlayer(name, death_tax);
                pl.sendMessage(String.format("You were fined %s for dying.", Format.money(death_tax)));
                if (main.getConfig().getBoolean("LogNotes", false)) {
                    log.info(Format.toAnsiColor(String.format("%s was fined %s for dying.", Format.username(pl.getName()), Format.money(death_tax))));
                }
            }
        }
    }
    
    private static boolean isStupidDeath(String death)
    {
        return (death.contains("fell") && !death.contains("world")) ||
               death.endsWith("drowned") ||
               death.endsWith("lava") ||
               death.contains("pricked") ||
               death.contains("suffocated");
    }
}
