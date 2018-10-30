package tbax.baxshops.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tbax.baxshops.Format;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.help.CommandHelp;

public class CmdGiveXp extends BaxShopCommand
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
    public CommandHelp getHelp()
    {
        return null;
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return ((actor.getNumArgs() == 3 && actor.isAdmin()) || actor.getNumArgs() == 2);
    }

    @Override
    public boolean hasPermission(ShopCmdActor actor)
    {
        if (actor.getNumArgs() == 3)
            return actor.isAdmin();
        return true;
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
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        int levels = actor.getArgInt(1, String.format(Resources.INVALID_DECIMAL, "number of XP levels"));

        if (levels < 0) {
            actor.exitError(String.format(Resources.INVALID_DECIMAL, "number of XP levels"));
        }
        else {
            Player p = actor.getPlayer();
            double money = levels * actor.getMain().getConfig().getDouble("XPConvert", 4d);

            Main.getEconomy().withdrawPlayer(p, money);
            p.setLevel(p.getLevel() + levels);

            p.sendMessage(String.format("You have been charged %s for %s levels", Format.money(money), Format.enchantments(levels + " XP")));
        }
    }
}
