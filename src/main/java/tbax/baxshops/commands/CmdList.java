/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.Format;
import tbax.baxshops.ShopSelection;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;

public final class CmdList extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "list";
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("List all locations for this shop");
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return true;
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
    public void onCommand(@NotNull ShopCmdActor actor)
    {
        ShopSelection selection = actor.getSelection();
        actor.sendMessage(Format.header("Shop Locations"));
        if (!selection.getShop().getLocations().isEmpty()) {
            int index = 1;
            actor.sendMessage(" %-3s %-16s %-18s", ChatColor.GRAY + "#", ChatColor.WHITE + "Location", ChatColor.WHITE + "Sign Text");
            for(Location loc : selection.getShop().getLocations()) {
                actor.sendMessage("%-3s %-16s %-18s %s",
                                ChatColor.WHITE.toString() + index + ".",
                                Format.location(loc),
                                ChatColor.LIGHT_PURPLE + selection.getShop().getSignTextString(loc),
                                (selection.getLocation().equals(loc) ? ChatColor.LIGHT_PURPLE + " (current)" : ""));
            }
        }
        else {
            actor.sendMessage(ChatColor.YELLOW + "This shop has no other locations.");
        }
    }
}
