/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.commands;

public class CommandErrorException extends PrematureAbortException
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
        return errMsg;
    }
}
