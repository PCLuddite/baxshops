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
package org.tbax.baxshops.internal.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.CommandHelpArgument;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.commands.CmdActor;
import org.tbax.baxshops.commands.CommandArgument;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.items.EnchantMap;
import org.tbax.baxshops.internal.text.*;

import java.util.List;
import java.util.Map;

public final class CmdInfo extends ShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "info";
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_TRADER_BUY;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "show more information about an entry");
        help.setLongDescription("Show extended information about an entry in the selected shop. It is recommended to use this before all major purchases. " +
                ChatColor.ITALIC + "Caveat emptor" + ChatColor.RESET);
        help.setArgs(
                new CommandHelpArgument("item", "the name or shop index of the entry", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 2;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresPlayer(@NotNull CmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxEntry entry = actor.getArg(1).asEntry();
        int index = actor.getShop().indexOf(entry) + 1;
        ChatComponent info = ChatComponent.of(Format.header("Entry Information"));
        info.append("\nName: ").append(ChatComponent.of(entry.getName())
            .hoverEvent(HoverEvent.showItem(entry.getItemStack()))
        );
        info.append("\nMaterial: ").append(entry.getType().toString());
        if (entry.getType().getMaxDurability() > 0) {
            info.append("\nDamage: ").append(entry.getDamagePercent() + "%", TextColor.YELLOW);
        }
        if (entry.getItemStack().hasItemMeta()) {
            ItemMeta meta = entry.getItemStack().getItemMeta();
            if (meta.hasDisplayName()) {
                info.append("\nDisplay Name: ").append(meta.getDisplayName(), TextColor.YELLOW);
            }
            if (meta.hasLore()) {
                info.append("\nDescription:");
                for (String line : meta.getLore()) {
                    info.append("\n" + line, TextColor.BLUE);
                }
            }
        }
        Map<Enchantment, Integer> enchmap = EnchantMap.getEnchants(entry.getItemStack());
        if (enchmap != null && !enchmap.isEmpty()) {
            info.append("\nEnchants: ").append(Format.enchantments(EnchantMap.fullListString(enchmap)));
        }
        info.append("\nQuantity: ").append(entry.getAmount() == 0 ? ChatColor.DARK_RED + "OUT OF STOCK" : Format.number(entry.getAmount()));
        if (entry.canBuy()) {
            info.append("\n");
            info.append(ChatComponent.of("[BUY]", TextColor.GREEN, ChatTextStyle.UNDERLINED)
                    .hoverEvent(HoverEvent.showText("Buy for " + entry.getFormattedBuyPrice()))
                    .clickEvent(ClickEvent.suggestCommand("/buy " + index + " "))
            );
            info.append(" ");
        }
        if (entry.canSell()) {
            info.append(ChatComponent.of("[SELL]", TextColor.BLUE, ChatTextStyle.UNDERLINED)
                    .hoverEvent(HoverEvent.showText("Sell for " + entry.getFormattedSellPrice()))
                    .clickEvent(ClickEvent.suggestCommand("/shop sellfrominventory " + index + " "))
            );
        }
        if (actor.getPlayer() == null) {
            info.sendTo(actor.getSender());
        }
        else {
            info.sendTo(actor.getPlayer());
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<? extends CommandArgument> args)
    {
        ShopCmdActor shopActor = (ShopCmdActor)actor;
        if (args.size() == 2 && shopActor.getShop() != null) {
            return shopActor.getShop().getAllItemAliases();
        }
        else {
            return super.onTabComplete(actor, command, alias, args);
        }
    }
}
