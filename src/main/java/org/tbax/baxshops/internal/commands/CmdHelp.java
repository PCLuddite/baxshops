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
package org.tbax.baxshops.internal.commands;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.CommandHelp;
import org.tbax.baxshops.CommandHelpArgument;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.commands.BaxShopCommand;
import org.tbax.baxshops.commands.ShopCmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.Resources;
import org.tbax.baxshops.internal.ShopPlugin;
import org.tbax.baxshops.internal.ShopSelection;
import org.tbax.baxshops.internal.text.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdHelp extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "help";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "h" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_TRADER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "show a list of commands");
        help.setArgs(
                new CommandHelpArgument("action", "get help on a /shop action, e.g. /shop h buy", false)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 1 || actor.getNumArgs() == 2;
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
        return false;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        if (actor.getNumArgs() == 1) {
            actor.appendArg(1); // show page 1 by default
        }
        if (actor.isArgInt(1)) {
            actor.getSender().sendMessage("Use this to lookup information on specific commands.");
            actor.getSender().sendMessage(String.format("To lookup a command, use: %s", Format.command("/shop help <command>")));
            showHelpList(actor, actor.getArgInt(1) - 1);
        }
        else {
            BaxShopCommand cmd = ShopPlugin.getCommands().get(actor.getArg(1));
            if (cmd == null) {
                actor.exitError(Resources.INVALID_SHOP_ACTION, actor.getArg(1));
            }
            CommandHelp help = cmd.getHelp(actor);
            if (cmd == null) {
                actor.exitError("No documentation could be found for /shop %s", actor.getArg(1));
            }
            else if (!cmd.hasPermission(actor)) {
                actor.sendError("You do not have permission to view the documentation for this command");
            }
            actor.getSender().sendMessage(help.toString().split("\\n"));
        }
    }

    private void showHelpList(@NotNull ShopCmdActor actor, int page)
    {
        List<BaxShopCommand> commands = ShopPlugin.getCommands().values().stream()
                .filter(cmd -> cmd.hasPermission(actor))
                .distinct()
                .sorted(Comparator.comparing(BaxShopCommand::getName))
                .collect(Collectors.toList());
        int pages = (int)Math.ceil((double)commands.size() / ShopSelection.ITEMS_PER_PAGE);
        Format.header(page + 1, pages, "/shop help").sendTo(actor.getSender());
        int i = page * ShopSelection.ITEMS_PER_PAGE,
                stop = (page + 1) * ShopSelection.ITEMS_PER_PAGE,
                max = Math.min(stop, commands.size());
        for (; i < max; ++i) {
            CommandHelp help = commands.get(i).getHelp(actor);
            ChatComponent.of("").append(
                    ChatComponent.of(help.getName(), TextColor.GOLD, ChatTextStyle.UNDERLINED)
                            .hoverEvent(HoverEvent.showText(ChatColor.GRAY + "Click for more info"))
                            .clickEvent(ClickEvent.runCommand("/shop help " + help.getName())))
                    .append(": ")
                    .append(help.getShortDescription())
            .sendTo(actor.getSender());
        }
        for (; i < stop; i++) {
            actor.getSender().sendMessage("");
        }
    }
}