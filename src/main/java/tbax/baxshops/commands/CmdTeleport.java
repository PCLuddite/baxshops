/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.CommandHelpArgument;
import tbax.baxshops.Format;
import tbax.baxshops.ShopSelection;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;

public final class CmdTeleport extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "teleport";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[]{"teleport","tp"};
    }

    @Override
    public String getPermission()
    {
        return "shops.admin";
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("Teleport to a specific shop location. Use /shop list for a list of locations");
        help.setArgs(
            new CommandHelpArgument("index", "the index of the shop location", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
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
        ShopSelection selection = actor.getSelection();

        int loc = actor.getArgInt(1, "Expected a location number. For a list of locations, use /shop list.");
        if (loc < 1 || loc > selection.getShop().getLocations().size()) {
            actor.exitError("That shop location does not exist.");
        }

        Location old = selection.getLocation();
        selection.setLocation(selection.getShop().getLocations().get(loc - 1));
        if (actor.getPlayer().teleport(selection.getLocation())) {
            actor.sendMessage("Teleported you to %s", Format.location(selection.getLocation()));
        }
        else {
            selection.setLocation(old);
            actor.exitError("Unable to teleport you to that location.");
        }
    }
}
