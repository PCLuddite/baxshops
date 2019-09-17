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
package org.tbax.baxshops;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.items.ItemUtil;
import org.tbax.baxshops.serialization.SafeMap;
import org.tbax.baxshops.serialization.StoredPlayer;
import org.tbax.baxshops.serialization.UpgradeableSerializable;
import org.tbax.baxshops.serialization.UpgradeableSerialization;
import org.tbax.baxshops.serialization.annotations.DoNotSerialize;
import org.tbax.baxshops.serialization.annotations.SerializeMethod;
import org.tbax.baxshops.serialization.states.State_00300;
import org.tbax.baxshops.serialization.states.State_00420;

import java.util.*;
import java.util.stream.Collectors;

public final class BaxShop implements UpgradeableSerializable, Collection<BaxEntry>
{
    public static final UUID DUMMY_UUID = UUID.fromString("8f289a15-cf9f-4266-b368-429cb31780ae");
    public static final BaxShop DUMMY_SHOP = new BaxShop(DUMMY_UUID);

    private UUID id = UUID.randomUUID();

    @Deprecated
    private String shortId = null;
    private String shortId2 = null;

    @SerializeMethod(getter = "getOwner")
    private UUID owner;

    private final Set<Location> locations = new HashSet<>();
    private final List<BaxEntry> inventory = new ArrayList<>();
    private int flags = BaxShopFlag.SELL_REQUESTS;

    @DoNotSerialize
    private long legacyId = Long.MIN_VALUE;

    private BaxShop(UUID uuid)
    {
        id = uuid;
        owner = StoredPlayer.DUMMY_UUID;
    }

    public BaxShop(Collection<? extends Location> locations)
    {
        id = UUID.randomUUID();
        owner = StoredPlayer.DUMMY_UUID;
        this.locations.addAll(locations);
    }

    public BaxShop(Location loc)
    {
        this(Collections.singleton(loc));
    }

    public BaxShop(Map<String, Object> args)
    {
        UpgradeableSerialization.upgrade(this, args);
    }

    public BaxShop(BaxShop shop)
    {
        id = shop.id;
        shortId = shop.shortId;
        shortId2 = shop.shortId2;
        owner = shop.owner;
        legacyId = shop.legacyId;
        locations.addAll(shop.locations);
        shop.stream().map(BaxEntry::new).forEach(inventory::add);
        flags = shop.flags;
    }

    @Override
    public void upgrade00300(@NotNull SafeMap map)
    {
        String name = map.getString("owner", StoredPlayer.DUMMY_NAME);
        id = UUID.randomUUID();
        legacyId = map.getLong("id");
        shortId2 = createShortId();
        owner = State_00300.getPlayerId(name);
        flags = State_00300.flagMapToFlag(map);
        inventory.addAll(map.getList("inventory"));
        locations.addAll(map.getList("locations"));
    }

    @Override
    public void upgrade00400(@NotNull SafeMap map)
    {
        id =  map.getUUID("id", UUID.randomUUID());
        shortId2 = createShortId();
        owner = map.getUUID("owner", StoredPlayer.ERROR_UUID);
        flags = State_00420.convertFlag(map.getInteger("flags"));
        inventory.addAll(map.getList("inventory"));
        locations.addAll(map.getList("locations"));
    }

    @Override
    public void upgrade00420(@NotNull SafeMap map)
    {
        id =  map.getUUID("id", UUID.randomUUID());
        shortId2 = createShortId();
        owner = map.getUUID("owner", StoredPlayer.ERROR_UUID);
        flags = map.getInteger("flags");
        inventory.addAll(map.getList("inventory"));
        locations.addAll(map.getList("locations"));
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

    @Deprecated
    public String getShortId()
    {
        return shortId;
    }

    public String getShortId2()
    {
        if (shortId2 == null)
            shortId2 = createShortId();
        return shortId2;
    }

    public void setShortId2(String shortId2)
    {
        this.shortId2 = shortId2;
    }

    private String createShortId()
    {
        String uuid = id.toString();
        int n = uuid.lastIndexOf('-') + 1;
        if (n >= uuid.length())
            throw new IllegalArgumentException();
        return uuid.substring(n);
    }

    public OfflinePlayer getOwner()
    {
        return ShopPlugin.getOfflinePlayer(getOwnerId());
    }

    public UUID getOwnerId()
    {
        if (owner == null)
            owner = StoredPlayer.ERROR_UUID;
        return owner;
    }

    public String getAbbreviatedOwnerName()
    {
        OfflinePlayer owner = getOwner();
        return owner.getName().length() < 13 ? owner.getName() : owner.getName().substring(0, 12) + '…';
    }

    public void setOwner(OfflinePlayer newOwner)
    {
        owner = newOwner.getUniqueId();
    }

    public int indexOf(BaxEntry entry)
    {
        for(int index = 0; index < inventory.size(); index++) {
            if (inventory.get(index).equals(entry, hasFlagSmartStack())) {
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
        return (int)Math.ceil((double) inventory.size() / ShopSelection.ITEMS_PER_PAGE);
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
            if (e.isSimilar(stack, hasFlagSmartStack())) {
                return e;
            }
        }
        return null;
    }

    public BaxEntry find(BaxEntry entry)
    {
        for (BaxEntry e : inventory) {
            if (e.isSimilar(entry, hasFlagSmartStack())) {
                return e;
            }
        }
        return null;
    }

    public ItemStack toItem(Location loc)
    {
        String[] lines = getSignText(loc);
        if (lines.length == 1) {
            return toItem(lines[0]);
        }
        else if (lines.length == 2) {
            return toItem(lines[0], lines[1]);
        }
        else if (lines.length == 3) {
            return toItem(lines[0], lines[1], lines[2]);
        }
        else if (lines.length >= 4) {
            return toItem(lines[0], lines[1], lines[2], lines[3]);
        }
        else {
            return toItem("");
        }
    }

    public ItemStack toItem()
    {
        return toItem(getAbbreviatedOwnerName() + "'s", "shop");
    }

    public ItemStack toItem(String line1)
    {
        return toItem(line1, "");
    }

    public ItemStack toItem(String line1, String line2)
    {
        return toItem(line1, line2, "");
    }

    public ItemStack toItem(String line1, String line2, String line3)
    {
        return toItem(line1, line2, line3, "");
    }

    public ItemStack toItem(String line1, String line2, String line3, String line4)
    {
        ItemStack item = ItemUtil.newDefaultSign();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.BLUE + line1);
        if (line2 != null && !"".equals(line2)) lore.add(ChatColor.BLUE + line2);
        if (line3 != null && !"".equals(line3)) lore.add(ChatColor.BLUE + line3);
        if (line4 != null && !"".equals(line4)) lore.add(ChatColor.BLUE + line4);
        lore.add(ChatColor.GRAY + "ID: " + getShortId2());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + getOwner().getName() + "'s shop");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
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

    public void setFlagSmartStack(boolean value)
    {
        flags = BaxShopFlag.setFlag(flags, BaxShopFlag.SMART_STACK, value);
    }

    public boolean hasFlagSmartStack()
    {
        return BaxShopFlag.hasFlag(flags, BaxShopFlag.SMART_STACK);
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
            return Resources.ERROR_INLINE;
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

    public List<String> getAllItemAliases()
    {
        return inventory.stream()
            .map(BaxEntry::getAlias)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    public static List<Block> getSignOnBlock(Block block)
    {
        List<Block> signs = new ArrayList<>();
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for(int z = -1; z <= 1; ++z) {
                    Location l = block.getLocation().add(x, y, z);
                    Block curr = l.getBlock();
                    if (ItemUtil.isSign(curr.getType())) {
                        if (curr.getBlockData() instanceof WallSign) {
                            WallSign sign = (WallSign)curr.getBlockData();
                            Block attached = curr.getRelative(sign.getFacing().getOppositeFace());
                            if (attached.getLocation().equals(block.getLocation())) {
                                signs.add(curr);
                            }
                        }
                        else {
                            Location below = l.subtract(0, 1, 0);
                            if (below.equals(block.getLocation())) {
                                signs.add(curr);
                            }
                        }
                    }
                }
            }
        }
        return signs;
    }

    public void sort(Comparator<? super BaxEntry> comparator) {
        inventory.sort(comparator);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaxShop that = (BaxShop) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
}
