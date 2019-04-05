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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.ItemNames;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.SavedState;
import tbax.baxshops.serialization.StoredPlayer;
import tbax.baxshops.serialization.states.State_00300;

import java.util.*;

public final class BaxShop implements ConfigurationSerializable, Collection<BaxEntry>
{
    public static final int ITEMS_PER_PAGE = 7;

    public static final UUID DUMMY_UUID = UUID.fromString("8f289a15-cf9f-4266-b368-429cb31780ae");
    public static final BaxShop DUMMY_SHOP = new BaxShop(DUMMY_UUID);
    
    private UUID id;
    private long legacyId = Long.MIN_VALUE;
    private UUID owner;
    private final Set<Location> locations = new HashSet<>();
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
        if (SavedState.getLoadedState() == State_00300.VERSION) {
            String name = map.getString("owner", StoredPlayer.DUMMY_NAME);
            id = UUID.randomUUID();
            legacyId = map.getLong("id");
            owner = State_00300.getPlayerId(name);
            flags = State_00300.flagMapToFlag(map);
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
    
    public Collection<Location> getLocations()
    {
        return Collections.unmodifiableCollection(locations);
    }

    public void removeLocation(Location loc)
    {
        locations.remove(loc);
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
        SafeMap map = new SafeMap();
        map.put("id", id);
        map.put("owner", getOwner());
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
