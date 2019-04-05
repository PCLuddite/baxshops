/*
 * Copyright (C) 2013-2019 Timothy Baxendale
 * Portions derived from Shops Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package tbax.baxshops;

import org.bukkit.*;
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
import tbax.baxshops.notification.DeathTaxReceivedNote;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.serialization.Configuration;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.Deque;
import java.util.UUID;

@SuppressWarnings("unused")
public class EventListener implements Listener
{
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event)
    {
        try {
            BaxShop shop = ShopPlugin.getShop(event.getBlock().getLocation());
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
                        ItemStack shopStack = shop.toItem(event.getBlock().getLocation());
                        event.getBlock().setType(Material.AIR);
                        ShopPlugin.removeLocation(shop.getId(), event.getBlock().getLocation());
                        ShopPlugin.clearSelection(event.getPlayer());
                        event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), shopStack);
                    }
                }
                else {
                    throw new CommandErrorException("You don't have permission to remove this shop.");
                }
            }
            Location above = event.getBlock().getLocation();
            above.setY(above.getY() + 1);
            if (ShopPlugin.getShop(above) != null) {
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
        
        BaxShop shop = ShopPlugin.getShop(b.getLocation());
        if (shop == null) {
            if (b.hasMetadata("shopid")) {
                shop = ShopPlugin.getShop(UUID.fromString(b.getMetadata("shopid").get(0).asString()));
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

        ShopSelection selection = ShopPlugin.getSelection(player);
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
            if (ShopPlugin.getShop(loc) != null) {
                event.setCancelled(true);
                return;
            }
            loc.setY(loc.getY() + 1);
            if (ShopPlugin.getShop(loc) != null) {
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
            if (!BaxShop.isShop(item))
                return;
            BaxShop shop = BaxShop.fromItem(item);
            if (shop == null) {
                throw new CommandErrorException("This shop has been closed and can't be placed.");
            }
            if (ShopPlugin.addLocation(event.getBlockPlaced().getLocation(), shop)){
                throw new CommandErrorException(Resources.SHOP_EXISTS);
            }
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
                    sign.setLine(1, lines[1]);
                    sign.setLine(2, lines[2]);
                    sign.setLine(3, lines.length > 3 ? lines[3] : "");
                }
                sign.update();
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
        BaxShop shop = ShopPlugin.getShop(event.getBlock().getLocation());
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
        ShopPlugin.getSavedState().joinPlayer(event.getPlayer());
        Deque<Notification> p = ShopPlugin.getSavedState().getNotifications(event.getPlayer());
        if (!p.isEmpty()) {
            event.getPlayer().sendMessage(ChatColor.WHITE + "You have new notifications. Use " + Format.command("/shop notifications") + ChatColor.WHITE + " to view them");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Player pl = event.getPlayer();
        ShopSelection s = ShopPlugin.getSelection(pl);
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
                ShopPlugin.clearSelection(event.getPlayer());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        if (!ShopPlugin.getSavedState().getConfig().isDeathTaxEnabled())
            return;
        if (!isStupidDeath(event.getEntity().getLastDamageCause().getCause()))
            return;

        Configuration config = ShopPlugin.getSavedState().getConfig();
        UUID uuid = config.getDeathTaxGoesToId();

        Player pl = event.getEntity();
        double minimum = config.getDeathTaxMinimum();
        double percent = config.getDeathTaxPercentage();
        if (ShopPlugin.getEconomy().has(pl, minimum)) {
            double death_tax = MathUtil.multiply(ShopPlugin.getEconomy().getBalance(pl), percent);
            if (config.getDeathTaxMaximum() > 0) {
                death_tax = Math.min(death_tax, config.getDeathTaxMaximum());
            }
            ShopPlugin.getEconomy().withdrawPlayer(pl, death_tax);
            ShopPlugin.sendInfo(pl, String.format("You were fined %s for dying.", Format.money(death_tax)));
            if (!uuid.equals(StoredPlayer.DUMMY_UUID)) { // do not deposit in dummy world account
                OfflinePlayer recipient = ShopPlugin.getOfflinePlayer(uuid);
                ShopPlugin.getEconomy().depositPlayer(recipient, death_tax);
                ShopPlugin.sendNotification(recipient, new DeathTaxReceivedNote(recipient, pl, event.getDeathMessage(), death_tax));
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
