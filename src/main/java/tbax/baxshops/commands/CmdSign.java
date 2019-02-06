/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import tbax.baxshops.CommandHelpArgument;
import tbax.baxshops.Resources;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;

public final class CmdSign extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "sign";
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
        help.setDescription("changes the text on a shop sign");
        help.setArgs(
            new CommandHelpArgument("text", "the new text of the sign, each line separated by |", true)
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
        if (!b.getType().equals(Material.SIGN) && !b.getType().equals(Material.SIGN_POST)) {
            ShopPlugin.getInstance().getLogger().warning(String.format(Resources.NOT_FOUND_SIGN, actor.getShop().getOwner()));
            actor.exitWarning("This shop is missing its sign.");
        }

        Sign sign = (Sign) b.getState();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < actor.getNumArgs(); ++i) {
            sb.append(actor.getArg(i));
            sb.append(" ");
        }
        int len = sb.length();
        if (len > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        if (len > 60) {
            actor.exitError("That text will not fit on the sign.");
        }
        String[] lines = sb.toString().split("\\|");
        for (int i = 0; i < lines.length; ++i) {
            if (lines[i].length() > 15) {
                actor.exitError("Line %d is too long. Lines can only be 15 characters in length.", i + 1);
            }
        }
        if (lines.length < 3) {
            sign.setLine(0, "");
            sign.setLine(1, lines[0]);
            sign.setLine(2, lines.length > 1 ? lines[1] : "");
            sign.setLine(3, "");
        }
        else {
            sign.setLine(0, lines[0]);
            sign.setLine(1, lines.length > 1 ? lines[1] : "");
            sign.setLine(2, lines.length > 2 ? lines[2] : "");
            sign.setLine(3, lines.length > 3 ? lines[3] : "");
        }
        sign.update();
    }
}
