/*
 * Copyright (C) 2013-2019 Timothy Baxendale
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package tbax.baxshops;

@SuppressWarnings("unused")
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
