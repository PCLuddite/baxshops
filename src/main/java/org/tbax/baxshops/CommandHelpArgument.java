/*
 * Copyright (C) 2013-2019 Timothy Baxendale
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.tbax.baxshops;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class CommandHelpArgument
{
    private String description;
    private String argument;
    private boolean required;
    private String defaultValue;

    public CommandHelpArgument(@NotNull String arg, @NotNull String desc, boolean req)
    {
        argument = arg;
        description = desc;
        required = req;
        defaultValue = "";
    }

    public CommandHelpArgument(@NotNull String arg, @NotNull String desc, Object defaultVal)
    {
        argument = arg;
        description = desc;
        required = false;
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
        return ChatColor.GOLD + argument + ChatColor.GRAY + ": " + description;
    }
}
