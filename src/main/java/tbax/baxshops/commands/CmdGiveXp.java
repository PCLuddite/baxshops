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
    public boolean argsValid(ShopCmdActor actor)
    {
        if (actor.getNumArgs() == 2 && actor.isAdmin()) {
            return true;
        }
        else if (actor.getNumArgs() != 1) {
            return false;
        }
        else if (actor.getPlayer() != null) {
            Main.sendError(actor.getSender(), "This command can only be used by a player");
            return false;
        }
        return true;
    }

    @Override
    public boolean requiresSelection()
    {
        return false;
    }

    @Override
    public boolean requiresOwner()
    {
        return false;
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        int levels = actor.getArgInt(1, String.format(Resources.INVALID_DECIMAL, "number of XP levels"));

        if (levels < 0) {
            Main.sendError(actor.getSender(), String.format(Resources.INVALID_DECIMAL, "number of XP levels"));
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
