package tbax.baxshops.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tbax.baxshops.Format;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.executer.CmdRequisite;
import tbax.baxshops.help.CommandHelp;

public class CmdTakeXp extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "takexp";
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
        return false;
    }

    @Override
    public boolean requiresSelection()
    {
        return false;
    }

    @Override
    public boolean requiresPlayer()
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
            actor.sendError(String.format(Resources.INVALID_DECIMAL, "number of XP levels"));
        }
        else {
            Player p = actor.getPlayer();
            double money = levels * actor.getMain().getConfig().getDouble("XPConvert", 4d);

            if (levels > p.getLevel()) {
                actor.sendError("You do not have enough experience for this exchange.");
            } else {
                Main.getEconomy().depositPlayer(p, money);
                p.setLevel(p.getLevel() - levels);

                p.sendMessage(String.format("You have exchanged %s levels for %s", Format.enchantments(levels + " XP"), Format.money(money)));
            }
        }
    }
}
