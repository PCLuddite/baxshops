/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class MathUtil
{
    private MathUtil()
    {
    }

    public static double roundedDouble(double n)
    {
        return Math.round(100d * n) / 100d;
    }

    public static double add(double a, double b)
    {
        return roundedDouble(a + b);
    }

    public static double subtract(double a, double b)
    {
        return roundedDouble(a - b);
    }

    public static double multiply(double a, double b)
    {
        return roundedDouble(a * b);
    }

    public static double divide(double a, double b)
    {
        return roundedDouble(a / b);
    }
}
