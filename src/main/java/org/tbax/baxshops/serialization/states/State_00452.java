/*
 * Copyright (C) 2013-2019 Timothy Baxendale
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
package org.tbax.baxshops.serialization.states;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.items.ItemUtil;
import org.tbax.baxshops.notification.HeadlessShopClaim;
import org.tbax.baxshops.notification.Notification;
import org.tbax.baxshops.serialization.SavedState;
import org.tbax.baxshops.serialization.StoredPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class State_00452 extends State_00451
{
    public static final double VERSION = 4.52;

    public State_00452(ShopPlugin plugin)
    {
        super(plugin);
    }

    private List<Notification> notes = new ArrayList<>();

    @Override
    public @NotNull Collection<BaxShop> buildShops(@NotNull FileConfiguration state)
    {
        List<BaxShop> shops = (List<BaxShop>)super.buildShops(state);
        for(int x = shops.size() - 1; x >= 0; --x) {
            BaxShop shop = shops.get(x);
            for (Location location : new ArrayList<>(shop.getLocations())) {
                if (!ItemUtil.isSign(location.getBlock().getType())) {
                    ShopPlugin.logWarning(String.format("Shop %s at %s is not a sign. This location will be removed.",
                        shop.getId().toString(), Format.location(location)
                    ));
                    shop.removeLocation(location);
                }
            }
            if (shop.getLocations().isEmpty() && !BaxShop.DUMMY_UUID.equals(shop.getId())) {
                if (shop.isEmpty()) {
                    ShopPlugin.logInfo(String.format("Shop %s has no locations and no inventory. This shop will be removed.", shop.getId().toString()));
                    shops.remove(shop);
                }
                else {
                    ShopPlugin.logWarning(String.format("Shop %s has no locations but still has inventory. A claim will be sent to the owner.", shop.getId().toString()));
                    notes.add(new HeadlessShopClaim(shop));
                }
            }
        }
        return shops;
    }

    @Override
    public SavedState loadState(@NotNull FileConfiguration state)
    {
        notes.clear();
        SavedState savedState = super.loadState(state);
        for(Notification note : notes) {
            StoredPlayer player = savedState.getOfflinePlayer(note.getRecipientId());
            if (!player.getNotifications().contains(note)) {
                player.queueNote(note);
            }
        }
        return savedState;
    }
}
