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
        return false;
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
    public void onCommand(ShopCmdActor actor)
    {
        CommandSender sender;
        if (cmd.getNumArgs() > 2 && cmd.isAdmin()) {
            sender = Bukkit.getPlayer(cmd.getArg(2));
        }
        else{
            sender = cmd.getSender();
        }

        if (!(sender instanceof Player)) {
            Main.sendError(sender, "This command can only be used by a player");
            return true;
        }

        int levels;
        try {
            levels = Integer.parseInt(cmd.getArg(1));
        }
        catch(NumberFormatException e) {
            Main.sendError(sender, String.format(Resources.INVALID_DECIMAL, "number of XP levels"));
            return true;
        }

        if (levels < 0) {
            Main.sendError(sender, String.format(Resources.INVALID_DECIMAL, "number of XP levels"));
            return true;
        }

        Player p;
        double money = levels * cmd.getMain().getConfig().getDouble("XPConvert", 4d);;
        p = (Player)sender;

        Main.getEconomy().withdrawPlayer(p, money);
        p.setLevel(p.getLevel() + levels);

        p.sendMessage(String.format("You have been charged %s for %s levels", Format.money(money), Format.enchantments(levels + " XP")));
    }
}
