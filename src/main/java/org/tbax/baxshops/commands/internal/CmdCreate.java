/*
 * Copyright (C) Timothy Baxendale
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
package org.tbax.baxshops.commands.internal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.*;
import org.tbax.baxshops.commands.BaxShopCommand;
import org.tbax.baxshops.commands.ShopCmdActor;
import org.tbax.baxshops.errors.CommandErrorException;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.PlayerUtil;
import org.tbax.baxshops.internal.Resources;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.internal.items.ItemUtil;
import org.tbax.baxshops.serialization.StoredPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdCreate extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "create";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"create","mk"};
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "create a shop");
        if (actor.isAdmin()) {
            help.setLongDescription("Creates a new shop with a sign from your inventory. You must specify a shop owner. " +
                            "The type of sign will be copied from a sign in your inventory. If no sign is found, the default sign type will be used.");
            help.setArgs(
                new CommandHelpArgument("owner", "the owner of the shop", true),
                new CommandHelpArgument("infinite", "whether the shop is infinite", "no")
            );
        }
        else {
            help.setLongDescription("Creates a new shop with a sign from your inventory with you as the owner");
        }
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        if (actor.isAdmin())
            return actor.getNumArgs() == 2 || actor.getNumArgs() == 3;
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresPlayer(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        OfflinePlayer owner;
        if (actor.isAdmin()) {
            owner = actor.getArgPlayerSafe(1);
        }
        else {
            owner = actor.getPlayer();
        }

        Location loc = actor.getPlayer().getLocation();
        loc = loc.getWorld().getBlockAt(loc).getLocation();
        assert actor.getInventory() != null;

        ItemStack sign = PlayerUtil.findSign(actor.getPlayer());
        if (!actor.isAdmin() && sign == null) {
            actor.exitError(Resources.NOT_FOUND_SIGN, "to set up a shop");
        }

        if (ShopPlugin.getShop(loc) != null)
            actor.exitError(Resources.SHOP_EXISTS);

        if (sign == null)
            sign = ItemUtil.newDefaultSign();

        Block b = loc.getBlock();
        BaxShop shop = new BaxShop(b.getLocation());
        shop.setOwner(owner);

        buildShopSign(loc, sign,
            "",
            shop.getAbbreviatedOwnerName() + "'s",
            "shop",
            ""
        );

        ShopPlugin.addShop(shop);

        if (actor.isAdmin() && actor.getNumArgs() == 3) {
            shop.setFlagInfinite(actor.getArgBoolean(2));
        }

        shop.setFlagSellRequests(shop.hasFlagInfinite());
        shop.setFlagBuyRequests(false);

        if (!actor.isAdmin()) {
            actor.getInventory().remove(sign.getType());
        }
        actor.sendMessage(Format.username(shop.getOwner().getName()) + "'s shop has been created.");
        actor.sendMessage(Format.flag("Buy requests") + " for this shop are " + Format.keyword(shop.hasFlagBuyRequests() ? "on" : "off"));
        actor.sendMessage(Format.flag("Sell requests") + " for this shop are " + Format.keyword(shop.hasFlagSellRequests() ? "on" : "off"));
    }

    private static void buildShopSign(@NotNull Location loc, @NotNull ItemStack sign, @NotNull String... signLines) throws PrematureAbortException
    {
        Location locUnder = loc.clone();
        locUnder.setY(locUnder.getY() - 1);

        Block b = loc.getWorld().getBlockAt(loc);
        Block blockUnder = locUnder.getWorld().getBlockAt(locUnder);
        if (blockUnder.getType() == Material.AIR || blockUnder.getType() == Material.TNT){
            throw new CommandErrorException("Sign does not have a block to place it on");
        }

        byte angle = (byte) ((((int) loc.getYaw() + 225) / 90) << 2);

        b.setType(sign.getType());
        loc.setYaw(angle);

        if (!ItemUtil.isSign(b.getType())) {
            throw new CommandErrorException(String.format("Unable to place sign! Block type is %s.", b.getType().toString()));
        }

        Sign signBlock = (Sign)b.getState();
        for(int i = 0; i < signLines.length; ++i) {
            signBlock.setLine(i, signLines[i]);
        }
        signBlock.update();

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        ShopCmdActor actor = (ShopCmdActor)sender;
        if (actor.isAdmin()) {
            if (actor.getNumArgs() == 2) {
                return ShopPlugin.getRegisteredPlayers().stream()
                    .map(StoredPlayer::getName)
                    .collect(Collectors.toList());
            }
            else if (actor.getNumArgs() == 3) {
                return Arrays.asList("true", "false");
            }
        }
        return super.onTabComplete(sender, command, alias, args);
    }
}
