/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.errors.PrematureAbortException;

public abstract class BaxShopCommand
{
    public abstract @NotNull String getName();
    public abstract String getPermission();

    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        return new CommandHelp(getName(), getAliases());
    }

    public abstract boolean hasValidArgCount(@NotNull ShopCmdActor actor);
    public abstract boolean requiresSelection(@NotNull ShopCmdActor actor);
    public abstract boolean requiresOwner(@NotNull ShopCmdActor actor);
    public abstract boolean requiresPlayer(@NotNull ShopCmdActor actor);
    public abstract boolean requiresItemInHand(@NotNull ShopCmdActor actor);
    public abstract void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException;

    public @NotNull String[] getAliases()
    {
        return new String[]{getName()};
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasPermission(@NotNull ShopCmdActor actor)
    {
        if (getPermission() == null)
            return true;
        return actor.hasPermission(getPermission());
    }

    public boolean hasAlternative(ShopCmdActor actor)
    {
        return false;
    }

    public @NotNull Class<? extends BaxShopCommand> getAlternative()
    {
        return this.getClass();
    }

    public final boolean requiresAdmin()
    {
        return "shops.admin".equalsIgnoreCase(getPermission());
    }
}
