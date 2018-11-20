/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.BaxEntry;
import tbax.baxshops.CommandHelpArgument;
import tbax.baxshops.Resources;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.CommandHelp;

public class CmdSetIndex extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "setindex";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"setindex","setorder","reorder"};
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public CommandHelp getHelp(ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("Change the order of an entry in the shop");
        help.setArgs(
            new CommandHelpArgument("old-index", "the current index of the item", true),
            new CommandHelpArgument("new-index", "the new index of the item", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 3;
    }

    @Override
    public boolean requiresSelection(ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresPlayer(ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        int oldIndex = actor.getShop().getIndexOfEntry(actor.getArgEntry(1));
        int newIndex = actor.getArgInt(2, String.format(Resources.INVALID_DECIMAL, "new index"));
        if (newIndex > actor.getShop().getInventorySize()) {
            actor.exitError( "You must choose a new index that is less than the number of items in the shop!");
        }
        if (newIndex < 1) {
            actor.exitError("The new index must be greater than 0.");
        }
        if (newIndex == oldIndex) {
            actor.exitWarning( "The index has not been changed.");
        }
        BaxEntry entry = actor.getShop().remove(oldIndex - 1);
        if (actor.getShop().getInventorySize() < newIndex) {
            actor.getShop().add(entry);
        }
        else {
            actor.getShop().add(newIndex - 1, entry);
        }
        actor.sendMessage("The index for this item was successfully changed.");
    }
}
