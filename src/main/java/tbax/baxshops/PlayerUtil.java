/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.CommandWarningException;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.notification.DeletedShopClaim;
import tbax.baxshops.notification.SaleClaim;
import tbax.baxshops.serialization.StoredData;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.*;

/**
 * Methods for dealing with interactions with players
 */
@SuppressWarnings("WeakerAccess")
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
        int fullStacks;
        item = item.clone();
        item.setAmount(Math.min(item.getAmount(), space));

        fullStacks = item.getAmount() / item.getMaxStackSize();
        item.setAmount(item.getAmount() - fullStacks * item.getMaxStackSize());

        if (item.getAmount() > 0)
            player.getInventory().addItem(item);

        while(fullStacks-- > 0) {
            ItemStack stack = item.clone();
            stack.setAmount(stack.getMaxStackSize());
            player.getInventory().addItem(stack);
        }

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

        if (!buyer.equals(StoredPlayer.DUMMY)) { // don't charge the dummy
            if (!ShopPlugin.getEconomy().has(buyer, price)) {
                throw new CommandErrorException(Resources.NO_MONEY_SELLER);
            }
            ShopPlugin.getEconomy().withdrawPlayer(buyer, price);
        }
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

    public static List<BaxEntry> takeQtyFromInventory(BaxQuantity qty) throws PrematureAbortException
    {
        return takeQtyFromInventory(qty, null, null);
    }

    public static List<BaxEntry> takeQtyFromInventory(BaxQuantity qty, BaxShop shop, Iterable<ItemStack> inv) throws PrematureAbortException
    {
        if (qty.isAny()) {
            if (shop == null || !(inv instanceof PlayerInventory))
                throw new CommandErrorException("'any' cannot be used for this action");
            return takeAnyFromInventory(shop, (PlayerInventory)inv);
        }

        BaxEntry clone = new BaxEntry(qty.getItem());
        clone.setAmount(takeFromInventory(inv, clone.getItemStack(), qty.getQuantity()));
        return Collections.singletonList(clone);
    }

    private static List<BaxEntry> takeAnyFromInventory(BaxShop shop, PlayerInventory inv)
    {
        ArrayList<BaxEntry> list = new ArrayList<>();

        if (shop == null)
            return list;

        for (BaxEntry entry : shop) {
            BaxEntry curr = new BaxEntry(entry);
            for (int x = 0; x < inv.getSize(); ++x) {
                ItemStack item = inv.getItem(x);
                if (curr.isSimilar(item)) {
                    curr.add(item.getAmount());
                    inv.setItem(x, null);
                }
            }
            if (curr.getAmount() > 0) {
                list.add(curr);
            }
        }

        return list;
    }

    public static int takeFromInventory(Iterable<ItemStack> stacks, ItemStack item, int amt)
    {
        int qty = 0;
        if (stacks instanceof Inventory) {
            Inventory inv = (Inventory)stacks;
            if (inv instanceof PlayerInventory) {
                ItemStack hand = ((PlayerInventory)inv).getItemInMainHand();
                if (hand != null && hand.isSimilar(item)) {
                    if (amt < hand.getAmount()) {
                        hand.setAmount(hand.getAmount() - amt);
                        qty += amt;
                    } else {
                        qty += hand.getAmount();
                        ((PlayerInventory)inv).setItemInMainHand(null);
                    }
                }
            }

            for (int x = 0; x < inv.getSize() && qty < amt; ++x) {
                ItemStack other = inv.getItem(x);
                if (other != null && other.isSimilar(item)) {
                    if (amt - qty < other.getAmount()) {
                        other.setAmount(other.getAmount() - (amt - qty));
                        qty = amt;
                    }
                    else {
                        qty += other.getAmount();
                        inv.setItem(x, null);
                    }
                }
            }
        }
        else {
            for(ItemStack invItem : stacks) {
                if (invItem.isSimilar(item)) {
                    if (amt - qty < invItem.getAmount()) {
                        invItem.setAmount(invItem.getAmount() - (amt - qty));
                        qty = amt;
                    }
                    else {
                        qty += invItem.getAmount();
                        invItem.setAmount(0);
                    }
                }
            }
        }

        return qty;
    }
}
