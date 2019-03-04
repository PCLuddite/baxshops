/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;

public final class CmdGiveXp extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "givexp";
    }

    @Override
    public String getPermission()
    {
        return null;
    }

    @Override
    public CommandHelp getHelp(ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("trade currency for XP");
        if (actor.isAdmin()) {
            help.setArgs(
                new CommandHelpArgument("levels", "the number of XP levels to give", true),
                new CommandHelpArgument("player", "the name of the player to trade currency", false)
            );
        }
        else {
            help.setArgs(
                new CommandHelpArgument("levels", "the number of XP levels to give", true)
                );
        }
        return help;
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return ((actor.getNumArgs() == 3 && actor.isAdmin()) || actor.getNumArgs() == 2);
    }

    @Override
    public boolean requiresSelection(ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresPlayer(ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresOwner(ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresItemInHand(ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        int levels = actor.getArgInt(1, String.format(Resources.INVALID_DECIMAL, "number of XP levels"));

        if (levels < 0) {
            actor.exitError(String.format(Resources.INVALID_DECIMAL, "number of XP levels"));
        }

        Player p;
        if (actor.isAdmin() && actor.getNumArgs() == 3) {
            OfflinePlayer offlinePlayer = actor.getArgPlayer(2);
            if (!offlinePlayer.isOnline())
                actor.exitError(Resources.NOT_ONLINE);
            p = offlinePlayer.getPlayer();
        }
        else {
            p = actor.getPlayer();
        }

        double money = levels * ShopPlugin.getInstance().getConfig().getDouble("XPConvert", 4d);
        if (!ShopPlugin.getEconomy().has(p, money)) {
            actor.sendError("You do not have enough funds to make this exchange");
        }
        ShopPlugin.getEconomy().withdrawPlayer(p, money);
        p.setLevel(p.getLevel() + levels);

        p.sendMessage(String.format("You have been charged %s for %s levels", Format.money(money), Format.enchantments(levels + " XP")));
    }
}
