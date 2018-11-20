/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops;

import org.bukkit.ChatColor;

public final class CommandHelpArgument
{
    private String description;
    private String argument;
    private boolean required;
    private String defaultValue = null;

    public CommandHelpArgument(String arg, String desc, boolean req)
    {
        this(arg, desc, req, null);
    }

    public CommandHelpArgument(String arg, String desc, boolean req, Object defaultVal)
    {
        argument = arg;
        description = desc;
        required = req;
        defaultValue = defaultVal + "";
    }

    public boolean isRequired()
    {
        return required;
    }

    public String getDescription()
    {
        return description;
    }

    public String getArgument()
    {
        return argument;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public String getUsageString()
    {
        if (required) {
            return String.format("<%s>", argument);
        }
        else {
            if (defaultValue.isEmpty()) {
                return String.format("[%s]", argument);
            }
            else {
                return String.format("[%s=%s]", argument, defaultValue);
            }
        }
    }

    public String toString()
    {
        return String.format("%s%s %s-%s %s",
            ChatColor.AQUA, argument,
            ChatColor.GRAY, ChatColor.WHITE,
            description
        );
    }
}
