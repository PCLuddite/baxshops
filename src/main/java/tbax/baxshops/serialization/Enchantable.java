/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization;

import tbax.baxshops.Format;

public final class Enchantable
{
    private String name;
    private boolean hasLevels;

    public Enchantable(String name, boolean hasLevels)
    {
        this.name = name;
        this.hasLevels = hasLevels;
    }

    public String getName()
    {
        return name;
    }

    public boolean hasLevels()
    {
        return hasLevels;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public String toString(int levels)
    {
        if (hasLevels)
            return name + " " + Format.toNumeral(levels);
        return name;
    }
}
