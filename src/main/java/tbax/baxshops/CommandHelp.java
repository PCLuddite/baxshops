/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class CommandHelp
{
    private String command;
    private String[] aliases;
    private CommandHelpArgument[] args;
    private String description;

    public CommandHelp(@NotNull String cmdName)
    {
        command = cmdName;
    }

    public CommandHelp(@NotNull String cmdName, String... aliases)
    {
        command = cmdName;
        this.aliases = aliases;
    }

    public @NotNull String getName()
    {
        return command;
    }

    public @NotNull String setName()
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

    public @NotNull String getUsageString()
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

    public @NotNull String getAliasString()
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
        if (description != null) {
            sb.append(ChatColor.WHITE).append(description).append('\n');
        }
        sb.append(getUsageString()).append('\n');
        if (aliases != null && aliases.length != 0) {
            sb.append(getAliasString()).append('\n');
        }
        if (args != null) {
            for (CommandHelpArgument arg : args) {
                sb.append('\n').append(arg);
            }
        }
        return sb.toString();
    }
}
