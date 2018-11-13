package tbax.baxshops.commands;

import org.bukkit.Location;
import tbax.baxshops.Format;
import tbax.baxshops.ShopSelection;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.help.CommandHelp;

public class CmdTeleport extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "teleport";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"teleport","tp"};
    }

    @Override
    public String getPermission()
    {
        return "shops.admin";
    }

    @Override
    public CommandHelp getHelp()
    {
        return new CommandHelp("teleport", "tp", "index",
                "Teleports the player to a specific shop location. Use /shop list for a list of shop locations.",
                CommandHelp.args("index", "the index of the shop location")
        );
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 2;
    }

    @Override
    public boolean requiresSelection(ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(ShopCmdActor actor)
    {
        return false;
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
