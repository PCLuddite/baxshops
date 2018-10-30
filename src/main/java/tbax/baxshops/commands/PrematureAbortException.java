/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

public class PrematureAbortException extends Exception
{
    private Exception innerEx;
    private String errMsg;

    public PrematureAbortException(String errorMsg)
    {
        errMsg = errorMsg;
    }

    public PrematureAbortException(Exception e, String errorMsg)
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
