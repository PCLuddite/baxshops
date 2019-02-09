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
import tbax.baxshops.serialization.StoredData;

import java.util.UUID;

/**
 * Methods for dealing with interactions with players
 */
public final class PlayerUtil
{
    private PlayerUtil()
    {
    }

    /**
     * gives an ItemStack to a player
     * @param player recipient of the ItemStack
     * @param item item to give the player
     * @return the amount that could not be added to the player's inventory
     * @throws PrematureAbortException thrown if no space is available
     */
    public static int giveItem(Player player, ItemStack item) throws PrematureAbortException
    {
        return giveItem(player, item, false);
    }

    /**
     * gives an ItemStack to a player
     * @param player recipient of the ItemStack
     * @param item item to give the player
     * @param allOrNothing if true, there must be space for the full amount of the ItemStack
     * @return the amount that could not be added to the player's inventory
     * @throws PrematureAbortException thrown if no space is available
     */
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

    /**
     * gets the amount of space in the player's inventory for a given item
     * @param player the player whose inventory to check
     * @param item item to check
     * @return the amount available to add in the player's inventory
     */
    public static int getSpaceForItem(Player player, ItemStack item)
    {
        ItemStack[] contents = player.getInventory().getStorageContents();
        int max = item.getMaxStackSize();
        int space = 0;

        for (ItemStack content : contents) {
            if (content == null || content.getType() == Material.AIR) {
                space += max;
            } else if (content.isSimilar(item)) {
                space += max - content.getAmount();
            }
        }
        return space;
    }

    /**
     * determines whether there is enough space available in a player's inventory for the full amount of an ItemStack
     * @param player the player whose inventory to check
     * @param item item to check
     * @return true if there is enough space, otherwise false
     */
    public static boolean hasRoomForItem(Player player, ItemStack item)
    {
        return item.getAmount() <= getSpaceForItem(player, item);
    }

    /**
     * gives an ItemStack to a player
     * @apiNote This differs from giveItem() in that it does not throw an exception, but sends the message to the player
     * @param player the player
     * @param item the item
     * @return true if the item was given, otherwise false
     */
    public static boolean tryGiveItem(Player player, ItemStack item)
    {
        try {
            giveItem(player, item, true);
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

    /**
     * Sells a BaxEntry to a player
     * @param shopId the UUID of the shop this entry is a part of
     * @param buyer the player who is purchasing the item
     * @param seller the player who is selling the item
     * @param entry the BaxEntry that is being sold
     * @throws PrematureAbortException thrown if the seller has insufficient funds
     */
    public static void sellItem(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry) throws PrematureAbortException
    {
        sellItem(StoredData.getShop(shopId), buyer, seller, entry);
    }

    /**
     * Sells a BaxEntry to a player
     * @param shop the shop this entry is a part of
     * @param buyer the player who is purchasing the item
     * @param seller the player who is selling the item
     * @param entry the BaxEntry that is being sold
     * @throws PrematureAbortException thrown if the seller has insufficient funds
     */
    public static void sellItem(BaxShop shop, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry) throws PrematureAbortException
    {
        double price = MathUtil.multiply(entry.getRefundPrice(), entry.getAmount());

        if (!ShopPlugin.getEconomy().has(buyer, price)) {
            throw new CommandErrorException(Resources.NO_MONEY_SELLER);
        }

        ShopPlugin.getEconomy().withdrawPlayer(buyer, price);
        ShopPlugin.getEconomy().depositPlayer(seller, price);

        if (shop == null) {
            DeletedShopClaim deletedClaim = new DeletedShopClaim(buyer, entry);
            ShopPlugin.sendNotification(buyer, deletedClaim);
        }
        else if (shop.hasFlagSellToShop()) {
            ItemStack item = entry.toItemStack();
            BaxEntry shopEntry = shop.find(item);
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
                ShopPlugin.sendNotification(buyer, claim);
            }
        }
    }

    /**
     * Sells a BaxEntry to a player
     * @param shopId the UUID of the shop this entry is a part of
     * @param buyer the UUID of the player who is purchasing the item
     * @param seller the UUID of the player who is selling the item
     * @param entry the BaxEntry that is being sold
     * @throws PrematureAbortException thrown if the seller has insufficient funds
     */
    public static void sellItem(UUID shopId, UUID buyer, UUID seller, BaxEntry entry) throws PrematureAbortException
    {
        sellItem(shopId, StoredData.getOfflinePlayer(buyer), StoredData.getOfflinePlayer(seller), entry);
    }
}
