/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.bukkit.ChatColor;
import tbax.baxshops.Format;
import tbax.baxshops.ShopSelection;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;

public final class CmdList extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "list";
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public CommandHelp getHelp(ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("List all locations for this shop");
        return help;
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresPlayer(ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        ShopSelection selection = actor.getSelection();

        actor.sendMessage(Format.header("Shop Locations"));
        if (!selection.getShop().getLocations().isEmpty()) {
            actor.sendMessage(" %-3s %-16s %-18s", ChatColor.GRAY + "#", ChatColor.WHITE + "Location", ChatColor.WHITE + "Sign Text");

            for(int index = 0; index < selection.getShop().getLocations().size(); ++index) {
                actor.sendMessage("%-3s %-16s %-18s %s",
                                ChatColor.WHITE.toString() + (index + 1) + ".",
                                Format.location(selection.getShop().getLocations().get(index)),
                                ChatColor.LIGHT_PURPLE + selection.getShop().getSignTextString(index),
                                (selection.getLocation().equals(selection.getShop().getLocations().get(index)) ? ChatColor.LIGHT_PURPLE + " (current)" : ""));
            }
        }
        else {
            actor.sendMessage(ChatColor.YELLOW + "This shop has no other locations.");
        }
    }
}
