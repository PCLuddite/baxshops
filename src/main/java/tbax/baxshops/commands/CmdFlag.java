/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Format;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.help.CommandHelp;

import java.util.Map;

public class CmdFlag extends BaxShopCommand
{
	private final Map<String, BaxShopCommand> flagCmds;
	
	public CmdFlag()
	{
		flagCmds = createCommandMap(
			FlagCmdSellToShop.class,
			FlagCmdInfinite.class,
			FlagCmdSellRequests.class,
			FlagCmdBuyRequests.class,
			FlagCmdOwner.class,
			FlagCmdList.class
		);
	}
	
    @Override
    public String getName()
    {
        return "flag";
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop flag", null, "<name|list> [setting]", "Set a specific flag or list all flags applied to a selected shop",
                CommandHelp.args("name", "the name of the flag to set",
                        "setting", "the option to set the flag",
                        "list", "lists all flags applied to the shop"));
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() >= 2 && flagCmds.get(actor.getArg(1)).hasValidArgCount(actor);
    }

    @Override
    public boolean requiresSelection(ShopCmdActor actor)
    {
        return actor.getNumArgs() >= 2 && flagCmds.get(actor.getArg(1)).requiresSelection(actor);
    }

    @Override
    public boolean requiresOwner(ShopCmdActor actor)
    {
        return actor.getNumArgs() >= 2 && flagCmds.get(actor.getArg(1)).requiresOwner(actor);
    }

    @Override
    public boolean requiresPlayer(ShopCmdActor actor)
    {
        return actor.getNumArgs() >= 2 && flagCmds.get(actor.getArg(1)).requiresPlayer(actor);
    }

    @Override
    public boolean requiresItemInHand(ShopCmdActor actor)
    {
        return actor.getNumArgs() >= 2 && flagCmds.get(actor.getArg(1)).requiresItemInHand(actor);
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
		flagCmds.get(actor.getArg(1)).onCommand(actor);
    }
	
	private abstract class FlagCmd extends BaxShopCommand
	{			
		@Override
		public String getName() { return getAliases()[0]; }

		@Override
		public boolean hasValidArgCount(ShopCmdActor actor) { return actor.getNumArgs() == 3; }
	
		@Override
		public String getPermission() { return "shops.owner"; }
		
		@Override
		public String[] getAliases() { return null; }

		@Override
		public CommandHelp getHelp() { return null; }

		@Override
		public boolean requiresSelection(ShopCmdActor actor) { return true; }

		@Override
		public boolean requiresOwner(ShopCmdActor actor) { return true; }

		@Override
		public boolean requiresPlayer(ShopCmdActor actor) { return true; }

		@Override
		public boolean requiresItemInHand(ShopCmdActor actor) { return false; }
	}
	
	private class FlagCmdSellToShop extends FlagCmd
	{
		@Override
		public String[] getAliases() { return new String[]{"selltoshop","sell_to_shop"}; }
		
		@Override
		public void onCommand(ShopCmdActor actor) throws PrematureAbortException
		{
			BaxShop shop = actor.getShop();
			boolean value = actor.getArgBoolean(2, "Usage:\n/shop flag sell_to_shop [true|false]");
			shop.setFlagSellToShop(value);
			actor.sendMessage(Format.flag("Sell to Shop") + " is " + Format.keyword(value ? "enabled" : "disabled"));
		}
	}
	
	private class FlagCmdInfinite extends FlagCmd
	{
		@Override
		public String[] getAliases() { return new String[]{"infinite", "isinfinite","inf"}; }
		
		@Override
		public void onCommand(ShopCmdActor actor) throws PrematureAbortException
		{
			BaxShop shop = actor.getShop();
			boolean value = actor.getArgBoolean(2, "Usage:\n/shop flag infinite [true|false]");
			shop.setFlagInfinite(value);
			for(BaxEntry e : shop)
			{
				e.setInfinite(value);
			}
			
			actor.sendMessage(Format.flag("Infinite items") + " for this shop are " + Format.keyword(value ? "enabled" : "disabled"));
		}
	}
	
	private class FlagCmdSellRequests extends FlagCmd
	{
		@Override
		public String[] getAliases() { return new String[]{"sellrequests","sellrequest","sell_request","sell_requests"}; }
		
		@Override
		public void onCommand(ShopCmdActor actor) throws PrematureAbortException
		{
			BaxShop shop = actor.getShop();
			boolean value = actor.getArgBoolean(2, "Usage:\n/shop flag sellrequests [true|false]");
			shop.setFlagSellRequests(value);
			actor.sendMessage(Format.flag("Sell requests") + " for this shop are " + Format.keyword(value ? "enabled" : "disabled"));
		}
	}
	
	private class FlagCmdBuyRequests extends FlagCmd
	{
		@Override
		public String[] getAliases() { return new String[]{"buyrequests","buyrequest","buy_request","buy_requests"}; }
		
		@Override
		public void onCommand(ShopCmdActor actor) throws PrematureAbortException
		{
			BaxShop shop = actor.getShop();
			boolean value = actor.getArgBoolean(2, "Usage:\n/shop flag buyrequests [true|false]");
			shop.setFlagBuyRequests(value);
			actor.sendMessage(Format.flag("Buy requests") + " for this shop are " + Format.keyword(value ? "enabled" : "disabled"));
		}
	}
	
	private class FlagCmdOwner extends FlagCmd
	{
		@Override
		public String[] getAliases() { return new String[]{"owner"}; }
		
		@Override
		public void onCommand(ShopCmdActor actor) throws PrematureAbortException
		{
			BaxShop shop = actor.getShop();
			shop.setOwner(actor.getArg(2));
			actor.sendMessage(Format.username(shop.getOwner()) + " is now the owner!");
			if (actor.isOwner()) {
				actor.sendMessage("You will still be able to edit this shop until you leave or reselect it.");
			}
		}
	}
	
	private class FlagCmdList extends FlagCmd
	{
		@Override
		public String[] getAliases() { return new String[]{"list"}; }
		
		@Override
		public boolean hasValidArgCount(ShopCmdActor actor) { return actor.getNumArgs() == 2; }
		
		@Override
		public void onCommand(ShopCmdActor actor) throws PrematureAbortException
		{
			BaxShop shop = actor.getShop();
			actor.sendMessage("\nFlags currently applied to this shop:");
			actor.sendMessage("%s: %s", Format.flag("Infinite"), Format.keyword(shop.hasFlagInfinite() ? "Yes" : "No"));
			actor.sendMessage("%s: %s", Format.flag("Notify"), Format.keyword(shop.hasFlagNotify() ? "Yes" : "No"));
			actor.sendMessage("%s: %s", Format.flag("Sell to Shop"), Format.keyword(shop.hasFlagSellToShop() ? "Yes" : "No"));
			actor.sendMessage("%s: %s", Format.flag("Sell Requests"), Format.keyword(shop.hasFlagSellRequests() ? "Yes" : "No"));
			actor.sendMessage("%s: %s", Format.flag("Buy Requests"), Format.keyword(shop.hasFlagBuyRequests() ? "Yes" : "No"));
		}
	}
}
