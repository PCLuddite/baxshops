/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.errors;

import org.bukkit.ChatColor;

public final class CommandWarningException extends PrematureAbortException
{
    private Exception innerEx;
    private String errMsg;

    public CommandWarningException(String errorMsg)
    {
        errMsg = errorMsg;
    }

    public CommandWarningException(Exception e, String errorMsg)
    {
        innerEx = e;
        errMsg = errorMsg;
    }

    public Exception getInnerException()
    {
        return innerEx;
    }

    public String getMessage()
    {
        return ChatColor.GOLD + errMsg;
    }
}
