/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *  Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus
 *
 *  +++====+++
**/

package tbax.baxshops;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.*;
import tbax.baxshops.serialization.states.State_30;

import java.util.*;

public final class BaxShop implements ConfigurationSerializable, Collection<BaxEntry>
{
    public static final int ITEMS_PER_PAGE = 7;

    public static final UUID DUMMY_UUID = UUID.fromString("8f289a15-cf9f-4266-b368-429cb31780ae");
    public static final BaxShop DUMMY_SHOP = new BaxShop(DUMMY_UUID);
    
    private UUID id;
    private long legacyId = Long.MIN_VALUE;
    private UUID owner;
    private final List<Location> locations = new ArrayList<>();
    private final List<BaxEntry> inventory = new ArrayList<>();

    private int flags = BaxShopFlag.NOTIFY | BaxShopFlag.SELL_REQUESTS;

    private BaxShop(UUID uuid)
    {
        id = uuid;
        owner = StoredPlayer.DUMMY_UUID;
    }

    public BaxShop()
    {
        id = UUID.randomUUID();
        owner = StoredPlayer.DUMMY_UUID;
    }

    public BaxShop(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        if (StoredData.getLoadedState() == State_30.VERSION) {
            String name = map.getString("owner", StoredPlayer.DUMMY_NAME);
            id = UUID.randomUUID();
            legacyId = map.getLong("id");
            owner = State_30.getPlayerId(name);
            flags = State_30.flagMapToFlag(map);
        }
        else {
            id =  map.getUUID("id", UUID.randomUUID());
            owner = map.getUUID("owner", StoredPlayer.ERROR_UUID);
            flags = map.getInteger("flags");
        }
        inventory.addAll(map.getList("inventory"));
        locations.addAll(map.getList("locations"));
    }

    public BaxShop(BaxShop shop)
    {
        id = shop.id;
        owner = shop.owner;
        legacyId = shop.legacyId;
        locations.addAll(shop.locations);
        shop.stream().map(BaxEntry::new).forEach(inventory::add);
        flags = shop.flags;
    }

    public UUID getId()
    {
        return id;
    }

    @Deprecated
    public long getLegacyId()
    {
        return legacyId;
    }

    public OfflinePlayer getOwner()
    {
        return ShopPlugin.getOfflinePlayer(owner);
    }

    public void setOwner(OfflinePlayer newOwner)
    {
        owner = newOwner.getUniqueId();
    }
    
    public int indexOf(BaxEntry entry)
    {
        for(int index = 0; index < inventory.size(); index++) {
            if (inventory.get(index).equals(entry)) {
                return index;
            }
        }
        return -1; // not found
    }
    
    public List<Location> getLocations()
    {
        return locations;
    }
    
    private static boolean compareLoc(Location a, Location b)
    {
        return a.getBlockX() == b.getBlockX() &&
               a.getBlockY() == b.getBlockY() &&
               a.getBlockZ() == b.getBlockZ() &&
               a.getWorld().equals(b.getWorld());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean removeLocation(Location loc)
    {
        for(int i = 0; i < locations.size(); ++i) {
            if (compareLoc(locations.get(i), loc)) {
                locations.remove(i);
                return true;
            }
        }
        return false;
    }
    
    public void addLocation(Location loc)
    {
        locations.add(loc);
    }

    public int getPages()
    {
        return (int)Math.ceil((double) inventory.size() / ITEMS_PER_PAGE);
    }

    @Override
    public int size()
    {
        return inventory.size();
    }

    @Override
    public boolean isEmpty()
    {
        return inventory.isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        if (o instanceof BaxEntry)
            return contains((BaxEntry)o);
        if (o instanceof ItemStack)
            return contains((ItemStack)o);
        return false;
    }

    public BaxEntry getEntry(String arg) throws PrematureAbortException
    {
        try {
            return getEntry(Integer.parseInt(arg) - 1);
        }
        catch (NumberFormatException e) {
            return ItemNames.getItemFromAlias(arg, this);
        }
        catch (IndexOutOfBoundsException e) {
            throw new CommandErrorException(e, Resources.NOT_FOUND_SHOPITEM);
        }
    }

    public BaxEntry getEntry(int index)
    {
        return inventory.get(index);
    }

    @Override
    public boolean add(BaxEntry entry)
    {
        return inventory.add(entry);
    }

    @Override
    public boolean remove(Object o)
    {
        if (o instanceof BaxEntry)
            return remove((BaxEntry)o);
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c)
    {
        return inventory.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends BaxEntry> c)
    {
        return inventory.addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c)
    {
        return inventory.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c)
    {
        return inventory.retainAll(c);
    }

    @Override
    public void clear()
    {
        inventory.clear();
    }

    public boolean contains(ItemStack stack)
    {
        return find(stack) != null;
    }

    public boolean contains(BaxEntry entry)
    {
        return indexOf(entry) > -1;
    }

    public BaxEntry find(ItemStack stack)
    {
        for (BaxEntry e : inventory) {
            if (e.isSimilar(stack)) {
                return e;
            }
        }
        return null;
    }

    public BaxEntry find(BaxEntry entry)
    {
        for (BaxEntry e : inventory) {
            if (e.isSimilar(entry)) {
                return e;
            }
        }
        return null;
    }

    public ItemStack toItem(Location loc)
    {
        ItemStack item = new ItemStack(Material.SIGN, 1);
        ArrayList<String> lore = new ArrayList<>();
        for(String line : getSignText(loc)) {
            lore.add(ChatColor.BLUE + line);
        }
        lore.add(ChatColor.GRAY + "ID: " + id);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + getOwner().getName() + "'s shop");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isShop(ItemStack item)
    {
        return item.getType() == Material.SIGN &&
               item.hasItemMeta() &&
               item.getItemMeta().hasLore() &&
               item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1).startsWith(ChatColor.GRAY + "ID: ");
    }

    public static BaxShop fromItem(ItemStack item)
    {
        UUID uid = UUID.fromString(item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1).substring((ChatColor.GRAY + "ID: ").length()));
        return ShopPlugin.getShop(uid);
    }

    public static String[] extractSignText(ItemStack item)
    {
        List<String> lore = item.getItemMeta().getLore().subList(0, item.getItemMeta().getLore().size() - 1);
        String[] lines = new String[lore.size()];
        for(int i = 0; i < lines.length; ++i) {
            lines[i] = ChatColor.stripColor(lore.get(i));
        }
        return lines;
    }

    public void setFlagBuyRequests(boolean value)
    {
        flags = BaxShopFlag.setFlag(flags, BaxShopFlag.BUY_REQUESTS, value);
    }

    public boolean hasFlagBuyRequests()
    {
        return !isWorldShop() && BaxShopFlag.hasFlag(flags, BaxShopFlag.BUY_REQUESTS);
    }

    public void setFlagSellRequests(boolean value)
    {
        flags = BaxShopFlag.setFlag(flags, BaxShopFlag.SELL_REQUESTS, value);
    }

    public boolean hasFlagSellRequests()
    {
        return !isWorldShop() && BaxShopFlag.hasFlag(flags, BaxShopFlag.SELL_REQUESTS);
    }

    public void setFlagNotify(boolean value)
    {
        flags = BaxShopFlag.setFlag(flags, BaxShopFlag.NOTIFY, value);
    }

    public boolean hasFlagNotify()
    {
        return !isWorldShop() && BaxShopFlag.hasFlag(flags, BaxShopFlag.NOTIFY);
    }

    public void setFlagSellToShop(boolean value)
    {
        flags = BaxShopFlag.setFlag(flags, BaxShopFlag.SELL_TO_SHOP, value);
    }

    public boolean hasFlagSellToShop()
    {
        return isWorldShop() || BaxShopFlag.hasFlag(flags, BaxShopFlag.SELL_TO_SHOP);
    }

    public void setFlagInfinite(boolean value)
    {
        flags = BaxShopFlag.setFlag(flags, BaxShopFlag.INFINITE, value);
    }

    public boolean hasFlagInfinite()
    {
        return BaxShopFlag.hasFlag(flags, BaxShopFlag.INFINITE);
    }

    public String getSignTextString(int index)
    {
        return getSignTextString(locations.get(index));
    }

    public String getSignTextString(Location loc)
    {
        try {
            Sign sign = (Sign)loc.getBlock().getState();
            StringBuilder ret = new StringBuilder();
            for(int line = 0; line < 4; line++) {
                if (!sign.getLine(line).isEmpty()) {
                    if (ret.length() > 0 && ret.charAt(ret.length() - 1) != '|') {
                        ret.append("|");
                    }
                    ret.append(sign.getLine(line));
                }
            }
            if (ret.length() == 0) {
                return ChatColor.RED + "<NO TEXT>";
            }
            else {
                return ChatColor.GREEN.toString() + (ret.length() > 15 ? ret.toString().substring(0,14) : ret);
            }
        }
        catch(Exception ex){
            return ChatColor.RED + Resources.ERROR_INLINE;
        }
    }

    public String[] getSignText(Location loc)
    {
        try {
            Sign sign = (Sign) loc.getBlock().getState();
            String[] allLines = sign.getLines();
            int start = 0, end = allLines.length - 1;
            while(start < end && allLines[start].isEmpty())
                ++start;
            while(end > start && allLines[end].isEmpty())
                --end;

            return Arrays.copyOfRange(allLines, start, end + 1);
        }
        catch (ClassCastException e) {
            return new String[0];
        }
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id.toString());
        map.put("owner", getOwner().getUniqueId().toString());
        map.put("flags", flags);
        map.put("inventory", inventory);
        map.put("locations", locations);
        return map;
    }
    
    public static BaxShop deserialize(Map<String, Object> args)
    {
        return new BaxShop(args);
    }
    
    public static BaxShop valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    @Override
    public Iterator<BaxEntry> iterator()
    {
        return inventory.iterator();
    }

    @Override
    public Object[] toArray()
    {
        return inventory.toArray();
    }

    @Override
    @SuppressWarnings("SuspiciousToArrayCall")
    public <T> T[] toArray(@NotNull T[] a)
    {
        return inventory.toArray(a);
    }

    @SuppressWarnings("UnusedReturnValue")
    public @NotNull Block buildShopSign(@NotNull Location loc, @NotNull String... signLines) throws PrematureAbortException
    {
        Location locUnder = loc.clone();
        locUnder.setY(locUnder.getY() - 1);

        Block b = loc.getWorld().getBlockAt(loc);
        Block blockUnder = locUnder.getWorld().getBlockAt(locUnder);
        if (blockUnder.getType() == Material.AIR || blockUnder.getType() == Material.TNT){
            throw new CommandErrorException("Sign does not have a block to place it on");
        }

        byte angle = (byte) ((((int) loc.getYaw() + 225) / 90) << 2);

        b.setType(Material.SIGN_POST);
        loc.setYaw(angle);

        if (!b.getType().equals(Material.SIGN)) {
            b.setType(Material.SIGN_POST);
            if (!(b.getType().equals(Material.SIGN) || b.getType().equals(Material.SIGN_POST))) {
                throw new CommandErrorException(String.format("Unable to place sign! Block type is %s.", b.getType().toString()));
            }
        }

        addLocation(b.getLocation());

        Sign sign = (Sign)b.getState();
        for(int i = 0; i < signLines.length; ++i) {
            sign.setLine(i, signLines[i]);
        }
        sign.update();

        return b;
    }

    public boolean remove(BaxEntry entry)
    {
        return inventory.remove(entry);
    }

    public BaxEntry removeEntryAt(int index)
    {
        return inventory.remove(index);
    }

    public void addEntry(int index, @NotNull BaxEntry entry)
    {
        inventory.add(index, entry);
    }

    public @NotNull Iterable<ItemStack> getItemStackInventory()
    {
        return () -> new Iterator<ItemStack>()
        {
            private int current = 0;

            @Override
            public boolean hasNext()
            {
                return current < size();
            }

            @Override
            public ItemStack next()
            {
                return inventory.get(current++).toItemStack();
            }
        };
    }

    public boolean isWorldShop()
    {
        return StoredPlayer.DUMMY.equals(getOwner());
    }
}
