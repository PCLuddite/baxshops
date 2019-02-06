/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.errors;

public final class CommandMessageException extends PrematureAbortException
{
    private Exception innerEx;
    private String errMsg;

    public CommandMessageException(String errorMsg)
    {
        errMsg = errorMsg;
    }

    public CommandMessageException(Exception e, String errorMsg)
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
        return errMsg;
    }
}
