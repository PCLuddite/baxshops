/*
 * Copyright (C) Timothy Baxendale
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
package org.tbax.baxshops.serialization.internal.states;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.internal.items.ItemUtil;
import org.tbax.baxshops.notification.internal.HeadlessShopClaim;
import org.tbax.baxshops.notification.Notification;
import org.tbax.baxshops.serialization.internal.State;
import org.tbax.baxshops.serialization.StoredPlayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StateLoader_00452 extends StateLoader_00451
{
    public static final double VERSION = 4.52;

    public StateLoader_00452(ShopPlugin plugin)
    {
        super(plugin);
    }

    private List<Notification> notes = new ArrayList<>();

    @Override
    public void sanitizeShopData(Collection<BaxShop> shops)
    {
        List<BaxShop> toRemove = new ArrayList<>();
        for(BaxShop shop : shops) {
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
                    toRemove.add(shop);
                }
                else {
                    if (StoredPlayer.DUMMY_UUID.equals(shop.getOwnerId())) {
                        ShopPlugin.logWarning(String.format("Shop %s owned by '%s' has no locations but still has inventory.", shop.getId().toString(), StoredPlayer.DUMMY_NAME));
                        ShopPlugin.logWarning("Ownership of this shop will be transferred to the error user and can be fixed manually in the configuration file.");
                        shop.setOwner(StoredPlayer.ERROR);
                    }
                    else if (!StoredPlayer.ERROR_UUID.equals(shop.getOwnerId())) {
                        ShopPlugin.logWarning(String.format("Shop %s has no locations but still has inventory. A claim will be sent to the owner if one hasn't been sent already.", shop.getId().toString()));
                    }
                    notes.add(new HeadlessShopClaim(shop));
                }
            }
        }
        shops.removeAll(toRemove);
    }

    @Override
    public State loadState(@NotNull File stateLocation) throws IOException, InvalidConfigurationException
    {
        notes.clear();
        State savedState = super.loadState(stateLocation);
        for(Notification note : notes) {
            StoredPlayer player = savedState.getOfflinePlayer(note.getRecipientId());
            if (!player.getNotifications().contains(note)) {
                player.queueNote(note);
            }
        }
        return savedState;
    }
}
