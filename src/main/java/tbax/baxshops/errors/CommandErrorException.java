/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.errors;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.Format;

@SuppressWarnings("unused")
public final class CommandErrorException extends PrematureAbortException
{
    private Exception innerEx;
    private String errMsg;

    public CommandErrorException(@NotNull String errorMsg)
    {
        errMsg = errorMsg;
    }

    public CommandErrorException(Exception e, @NotNull String errorMsg)
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
        return Format.error(errMsg);
    }
}
