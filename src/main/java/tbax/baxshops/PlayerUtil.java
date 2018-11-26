/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.CommandWarningException;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.notification.DeletedShopClaim;
import tbax.baxshops.notification.SaleClaim;
import tbax.baxshops.serialization.SavedData;

import java.util.UUID;

public class PlayerUtil
{
    public static int giveItem(Player player, ItemStack item) throws PrematureAbortException
    {
        return giveItem(player, item, false);
    }

    public static int giveItem(Player player, ItemStack item, boolean allOrNothing) throws PrematureAbortException
    {
        int space = getSpaceForItem(player, item);
        if (space == 0 || (allOrNothing && space < item.getAmount())) {
            throw new CommandErrorException(Resources.NO_ROOM);
        }

        int overflow = Math.max(item.getAmount() - space, 0);
        item = item.clone();
        item.setAmount(Math.min(item.getAmount(), space));
        player.getInventory().addItem(item);
        return overflow;
    }

    public static int getSpaceForItem(Player player, ItemStack stack)
    {
        ItemStack[] contents = player.getInventory().getStorageContents();
        int max = stack.getMaxStackSize();
        int space = 0;

        for(int x = 0; x < contents.length; ++x) {
            if (contents[x] == null || contents[x].getType() == Material.AIR) {
                space += max;
            }
            else if (contents[x].isSimilar(stack)) {
                space += max - contents[x].getAmount();
            }
        }
        return space;
    }

    public static boolean hasRoomForItem(Player player, ItemStack stack)
    {
        return stack.getAmount() <= getSpaceForItem(player, stack);
    }

    public static boolean tryGiveItem(Player player, ItemStack stack)
    {
        try {
            giveItem(player, stack, true);
            return true;
        }
        catch (CommandErrorException | CommandWarningException e) {
            player.sendMessage(e.getMessage());
            return false;
        }
        catch (PrematureAbortException e) {
            player.sendMessage(e.getMessage());
            return true;
        }
    }

    public static void sellItem(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry) throws PrematureAbortException
    {
        sellItem(SavedData.getShop(shopId), buyer, seller, entry);
    }

    public static void sellItem(BaxShop shop, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry) throws PrematureAbortException
    {
        double price = MathUtil.multiply(entry.getRefundPrice(), entry.getAmount());

        if (!Main.getEconomy().has(buyer, price)) {
            throw new CommandErrorException(Resources.NO_MONEY_SELLER);
        }

        Main.getEconomy().withdrawPlayer(buyer, price);
        Main.getEconomy().depositPlayer(seller, price);

        if (shop == null) {
            DeletedShopClaim deletedClaim = new DeletedShopClaim(buyer, entry.clone());
            SavedData.sendNotification(buyer, deletedClaim);
        }
        else if (shop.hasFlagSellToShop()) {
            ItemStack item = entry.toItemStack();
            BaxEntry shopEntry = shop.findEntry(item);
            if (shopEntry == null) {
                shopEntry = new BaxEntry();
                shopEntry.setItem(item);
            }
            if (shop.hasFlagInfinite()) {
                shopEntry.setInfinite(true);
            }
            else {
                shopEntry.add(item.getAmount());
            }
        }
        else {
            ItemStack item = entry.toItemStack();
            if (buyer.isOnline() && hasRoomForItem(buyer.getPlayer(), item)) {
                giveItem(buyer.getPlayer(), item, true);
            }
            else {
                SaleClaim claim = new SaleClaim(shop.getId(), buyer, seller, entry);
                SavedData.sendNotification(buyer, claim);
            }
        }
    }
}
