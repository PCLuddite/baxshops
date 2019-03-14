/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.errors;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class CommandMessageException extends PrematureAbortException
{
    private Exception innerEx;
    private String errMsg;

    public CommandMessageException(@NotNull String errorMsg)
    {
        errMsg = errorMsg;
    }

    public CommandMessageException(Exception e, @NotNull String errorMsg)
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
        return errMsg;
    }
}
