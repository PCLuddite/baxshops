package tbax.baxshops.commands;

public class PrematureAbortException extends Exception
{
    private Exception innerEx;

    public PrematureAbortException(Exception e)
    {
        innerEx = e;
    }

    public Exception getInnerException()
    {
        return innerEx;
    }
}
