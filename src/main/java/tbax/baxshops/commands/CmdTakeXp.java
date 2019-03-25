/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;

public final class CmdTakeXp extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "takexp";
    }

    @Override
    public String getPermission()
    {
        return null;
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("trade XP for currency");
        if (actor.isAdmin()) {
            help.setArgs(
                new CommandHelpArgument("levels", "the number of XP levels to take", true),
                new CommandHelpArgument("player", "the name of the player to trade currency", false)
            );
        }
        else {
            help.setArgs(
                new CommandHelpArgument("levels", "the number of XP levels to take", true)
            );
        }
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return ((actor.getNumArgs() == 3 && actor.isAdmin()) || actor.getNumArgs() == 2);
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
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
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        int levels = actor.getArgInt(1, String.format(Resources.INVALID_DECIMAL, "number of XP levels"));

        if (levels < 0) {
            actor.exitError(Resources.INVALID_DECIMAL, "number of XP levels");
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

        if (levels > p.getLevel()) {
            actor.exitError("You do not have enough experience for this exchange.");
        } else {
            ShopPlugin.getEconomy().depositPlayer(p, money);
            p.setLevel(p.getLevel() - levels);

            p.sendMessage(String.format("You have exchanged %s levels for %s", Format.enchantments(levels + " XP"), Format.money(money)));
        }
    }
}
