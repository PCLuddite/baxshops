/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.commands;

import org.bukkit.inventory.ItemStack;
import tbax.baxshops.*;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.ItemNames;

public class CmdRemove extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "remove";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"remove","rm"};
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
        help.setDescription("remove an item from the shop");
        help.setArgs(
            new CommandHelpArgument("item", "the name or entry number of the item to remove", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 2;
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
        BaxShop shop = actor.getShop();
        BaxEntry entry = actor.getArgEntry(1);

        if (entry == null) {
            actor.exitError(Resources.NOT_FOUND_SHOPITEM);
        }

        if (!shop.hasFlagInfinite() && entry.getAmount() > 0) {
            ItemStack stack = entry.toItemStack();
            if (!actor.hasRoomForItem(stack)) {
                actor.exitError(Resources.NO_ROOM);
            }

            actor.sendMessage("%s %s added to your inventory.",
                Format.itemname(entry.getAmount(), ItemNames.getName(entry)),
                entry.getAmount() == 1 ? "was" : "were");
        }

        shop.remove(entry);
        actor.sendMessage("The shop entry was removed.");
    }
}
