/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.commands;

import org.bukkit.block.Block;
import tbax.baxshops.CommandHelpArgument;
import tbax.baxshops.Format;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;

public class CmdSetAngle extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "setangle";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"setangle","setface","face"};
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
        help.setDescription("rotates a shop sign to face another direction");
        help.setArgs(
            new CommandHelpArgument("direction", "A cardinal direction (i.e., 'north', 'south', 'east' or 'west')", true)
        );
        return help;
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
        Block b = actor.getSelection().getLocation().getBlock();

        byte angle = 0;
        try {
            angle = (byte)((Integer.parseInt(actor.getArg(1)) % 4) << 2);
        }
        catch(NumberFormatException e) {
            switch(actor.getArg(1).toLowerCase()) {
                case "south": angle = 0; break;
                case "west": angle = 1; break;
                case "north": angle = 2; break;
                case "east": angle = 3; break;
                default:
                    actor.exitError("The direction you entered wasn't valid! Use one of the four cardinal directions.");
            }
            angle = (byte)(angle << 2);
        }
        try {
            b.setData(angle, false);
            actor.sendMessage("Sign rotated to face " + Format.keyword(actor.getArg(1).toLowerCase()));
        }
        catch(Exception e) {
            actor.exitError("An error occurred when trying to rotate the sign. The sign may not have been rotated");
        }
    }
}
