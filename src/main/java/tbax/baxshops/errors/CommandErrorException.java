/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.errors;

import org.bukkit.ChatColor;

@SuppressWarnings("unused")
public final class CommandErrorException extends PrematureAbortException
{
    private Exception innerEx;
    private String errMsg;

    public CommandErrorException(String errorMsg)
    {
        errMsg = errorMsg;
    }

    public CommandErrorException(Exception e, String errorMsg)
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
        return ChatColor.RED + errMsg;
    }
}
