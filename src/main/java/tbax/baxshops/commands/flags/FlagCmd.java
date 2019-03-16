/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands.flags;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.commands.BaxShopCommand;
import tbax.baxshops.commands.ShopCmdActor;

public abstract class FlagCmd extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return getAliases()[0];
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() == 3;
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @NotNull
    @Override
    public String[] getAliases()
    {
        return null;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresPlayer(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    public abstract boolean requiresRealOwner(@NotNull ShopCmdActor actor);
}