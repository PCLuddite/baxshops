/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.CommandWarningException;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.serialization.StoredData;

import java.util.ArrayDeque;
import java.util.UUID;

public class EventListener implements Listener
{
    private final Main plugin;
    
    public EventListener(Main plugin)
    {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event)
    {
        try {
            BaxShop shop = StoredData.getShop(event.getBlock().getLocation());
            if (shop != null) {
                if (shop.getOwner().equals(event.getPlayer()) || event.getPlayer().hasPermission("shops.admin")) {
                    if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                        event.setCancelled(true);
                    }
                    else if (shop.getLocations().size() == 1) {
                        throw new CommandWarningException("This is the only location for this shop. It cannot be destroyed.");
                    }
                    else {
                        event.setCancelled(true);
                        event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), shop.toItem(event.getBlock().getLocation()));
                        StoredData.removeLocation(null, event.getBlock().getLocation()); // we don't need to tell the player if there's an error 12/5/15
                        Main.clearSelection(event.getPlayer());
                        event.getBlock().setType(Material.AIR);
                    }
                }
                else {
                    throw new CommandErrorException("You don't have permission to remove this shop.");
                }
            }
            Location above = event.getBlock().getLocation();
            above.setY(above.getY() + 1);
            if (StoredData.getShop(above) != null) {
                throw new CommandWarningException("You cannot break this block because there is a shop above it.");
            }
        }
        catch (PrematureAbortException e) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Block b = event.getClickedBlock();
        Player player = event.getPlayer();

        if (b == null) {
            return;
        }
        
        BaxShop shop = StoredData.getShop(b.getLocation());
        if (shop == null) {
            if (b.hasMetadata("shopid")) {
                shop = StoredData.getShop(UUID.fromString(b.getMetadata("shopid").get(0).asString()));
                if (shop == null) {
                    event.getPlayer().sendMessage("This shop has been closed.");
                    return;
                }
            }
            else {
                return;
            }
        }

        boolean isOwner = shop.getOwner().equals(player);

        ShopSelection selection = Main.getSelection(player);
        selection.setLocation(b.getLocation());

        if (selection.getShop() == shop) {
            int pages = shop.getPages();
            if (pages == 0) {
                selection.setPage(0);
            }
            else {
                int delta = event.getAction() == Action.LEFT_CLICK_BLOCK ? 1 : -1;
                selection.setPage((((selection.getPage() + delta) % pages) + pages) % pages);
            }
            player.sendMessage("");
            player.sendMessage("");
        }
        else {
            selection.setIsOwner(isOwner);
            selection.setShop(shop);
            selection.setPage(0);
            StringBuilder intro = new StringBuilder(ChatColor.WHITE.toString());
            intro.append("Welcome to ");
            if (isOwner) {
                intro.append(Format.username("your"));
            }
            else {
                intro.append(Format.username(shop.getOwner().getName())).append("'s");
            }
            intro.append(" shop\n");
            intro.append(ChatColor.GRAY.toString());
            intro.append("For help with shops, type /shop help.");
            player.sendMessage(intro.toString());
        }

        selection.showListing(player);
    }
	
    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplosion(EntityExplodeEvent event)
    {
        for (Block b : event.blockList()) {
            Location loc = b.getLocation();
            if (StoredData.getShop(loc) != null) {
                event.setCancelled(true);
                return;
            }
            loc.setY(loc.getY() + 1);
            if (StoredData.getShop(loc) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        try {
            ItemStack item = event.getItemInHand();
            if (BaxShop.isShop(item)) {
                BaxShop shop = BaxShop.fromItem(item);
                if (shop == null) {
                    throw new CommandWarningException("This shop has been closed and can't be placed.");
                } else {
                    StoredData.addLocation(event.getPlayer(), event.getBlockPlaced().getLocation(), shop);
                    shop.addLocation(event.getBlockPlaced().getLocation());
                    String[] lines = BaxShop.extractSignText(item);
                    if (lines.length > 0) {
                        Sign sign = (Sign) event.getBlockPlaced().getState();
                        if (lines.length < 3) {
                            sign.setLine(0, "");
                            sign.setLine(1, lines[0]);
                            sign.setLine(2, lines.length > 1 ? lines[1] : "");
                            sign.setLine(3, "");
                        } else {
                            sign.setLine(0, lines[0]);
                            sign.setLine(1, lines.length > 1 ? lines[1] : "");
                            sign.setLine(2, lines.length > 2 ? lines[2] : "");
                            sign.setLine(3, lines.length > 3 ? lines[3] : "");
                        }
                        sign.update();
                    }
                }
            }
        }
        catch (PrematureAbortException e) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent event)
    {
        BaxShop shop = StoredData.getShop(event.getBlock().getLocation());
        if (shop != null) {
            for(String line : event.getLines()) {
                if (!line.isEmpty()) {
                    return;
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        StoredData.joinPlayer(event.getPlayer());
        ArrayDeque<Notification> p = StoredData.getNotifications(event.getPlayer());
        if (!p.isEmpty()) {
            event.getPlayer().sendMessage(ChatColor.WHITE + "You have new notifications. Use " + Format.command("/shop notifications") + ChatColor.WHITE + " to view them");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Player pl = event.getPlayer();
        ShopSelection s = Main.getSelection(pl);
        if (s.getShop() != null) {
            Location shopLoc = s.getLocation();
            Location pLoc = event.getTo();
            if (shopLoc.getWorld() != pl.getWorld() || shopLoc.distanceSquared(pLoc) > Resources.SHOP_RANGE) {
                if (s.isOwner()) {
                    pl.sendMessage("[Left " + Format.username("your") + " shop]");
                }
                else {
                    pl.sendMessage("[Left " + Format.username(s.getShop().getOwner().getName()) + "'s shop]");
                }
                Main.clearSelection(event.getPlayer());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        String name = plugin.getConfig().getString("DeathTax.GoesTo", null);
        if (name != null) {
            Player pl = event.getEntity();
            if (Main.getEconomy().has(pl, 100.00) && isStupidDeath(pl.getLastDamageCause().getCause())) {
                double death_tax = Main.getEconomy().getBalance(pl) * plugin.getConfig().getDouble("DeathTax.Percentage", 0.04);
                Main.getEconomy().withdrawPlayer(pl, death_tax);
                Main.getEconomy().depositPlayer(name, death_tax);
                pl.sendMessage(String.format("You were fined %s for dying.", Format.money(death_tax)));
                if (plugin.getConfig().getBoolean("LogNotes", false)) {
                    Main.getLog().info(Format.toAnsiColor(String.format("%s was fined %s for dying.", Format.username(pl.getName()), Format.money(death_tax))));
                }
            }
        }
    }
    
    private static boolean isStupidDeath(DamageCause death)
    {
        return death == DamageCause.FALL || death == DamageCause.DROWNING ||
               death == DamageCause.LAVA || death == DamageCause.CONTACT ||
               death == DamageCause.FIRE || death == DamageCause.FIRE_TICK ||
               death == DamageCause.SUFFOCATION;
    }
}
