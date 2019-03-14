/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.errors;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.Format;

@SuppressWarnings("unused")
public final class CommandWarningException extends PrematureAbortException
{
    private Exception innerEx;
    private String errMsg;

    public CommandWarningException(@NotNull String errorMsg)
    {
        errMsg = errorMsg;
    }

    public CommandWarningException(Exception e, @NotNull String errorMsg)
    {
        innerEx = e;
        errMsg = errorMsg;
    }

    public Exception getInnerException()
    {
        return innerEx;
    }

    public @NotNull String getMessage()
    {
        return Format.warning(errMsg);
    }
}
