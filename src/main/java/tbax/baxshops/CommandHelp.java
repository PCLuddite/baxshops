/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops;

import org.bukkit.ChatColor;

public final class CommandHelp
{
    private String command;
    private String[] aliases;
    private CommandHelpArgument[] args;
    private String description;

    public CommandHelp(String cmdName)
    {
        command = cmdName;
    }

    public CommandHelp(String cmdName, String... aliases)
    {
        command = cmdName;
        this.aliases = aliases;
    }

    public String getName()
    {
        return command;
    }

    public String setName()
    {
        return command;
    }

    public String[] getAliases()
    {
        return aliases;
    }

    public void setAliases(String... aliases)
    {
        this.aliases = aliases;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String desc)
    {
        description = desc;
    }

    public CommandHelpArgument[] getArgs()
    {
        return args;
    }

    public void setArgs(CommandHelpArgument... args)
    {
        this.args = args;
    }

    public String getUsageString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.AQUA).append("Usage: ").append(ChatColor.WHITE).append("/shop ").append(command);
        if (args != null) {
            for (CommandHelpArgument arg : args) {
                sb.append(" ").append(arg.getUsageString());
            }
        }
        return sb.toString();
    }

    public String getAliasString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.AQUA);
        for(String alias : aliases) {
            if (!command.equalsIgnoreCase(alias)) {
                sb.append(" ").append(alias);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Format.header(String.format("Help: /shop %s", command))).append('\n');
        sb.append(ChatColor.WHITE).append(description).append('\n');
        sb.append(getUsageString()).append('\n');
        if (aliases != null && aliases.length != 0) {
            sb.append(getAliasString()).append('\n');
        }
        for(CommandHelpArgument arg : args) {
            sb.append('\n').append(arg);
        }
        return sb.toString();
    }
}
