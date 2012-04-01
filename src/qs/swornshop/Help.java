package qs.swornshop;

import java.util.HashMap;

import org.bukkit.command.CommandSender;

public class Help {
	
	public static final CommandHelp help = new CommandHelp("shop help", "h", "[action]", "show help with shops",
			CommandHelp.arg("action", "get help on a /shop action, e.g. /shop h create"));
	public static final CommandHelp create = new CommandHelp("shop create", "c", "<owner>", "create a new shop", 
			CommandHelp.arg("owner", "the owner of the shop"));
	public static final CommandHelp remove = new CommandHelp("shop remove", "rm", null, "removes this shop");
	
	public static final CommandHelp pending = new CommandHelp("shop pending", "p", null, "view pending shop requests", 
			"Shows a list of pending offers to sell items to your shops",
			"Use /shop accept and /shop reject on these offers.");
	
	public static final CommandHelp buy = new CommandHelp("shop buy", "b", "<item> <quantity>", "buy an item from this shop", 
			CommandHelp.args(
				"item", "the name of the item",
				"quantity", "the quantity you wish to buy"
			));
	public static final CommandHelp sell = new CommandHelp("shop sell", "s", "<item> <quantity> [price=auto]", "request to sell an item to this shop",
			CommandHelp.args(
				"item", "the name of the item",
				"quantity", "the quantity you wish to sell",
				"price", "the price (for the entire quantity); defaults to the store's price times the quantity"
			));
	
	public static final CommandHelp add = new CommandHelp("shop add", "a", "<buy-price> [sell-price=none]", "add your held item to this shop",
			concat(CommandHelp.args(
				"buy-price", "the price of a single item in the stack",
				"sell-price", "the selling price of a single item in the stack (by default the item cannot be sold)"
			), new String[] {
				"§BWarning:§F Once you add an item to a shop, you cannot remove it."
			}));
	public static final CommandHelp restock = new CommandHelp("shop restock", "r", null, "restock this shop with your held item");
	
	public static final CommandHelp set = new CommandHelp("shop set", null, "<item> <buy-price> <sell-price>", "changes the price of an item",
			CommandHelp.args(
				"item", "the ID or name of the item to modify",
				"buy-price", "the new price of a single item in the stack",
				"sell-price", "the selling price of a single item in the stack (by default the item cannot be sold)"
			));
	
	public static final CommandHelp lookup = new CommandHelp("shop lookup", null, "<item-name>", "look up an item's ID and damage value",
			CommandHelp.arg("item-name", "the name of an alias for an item"));
	
	public static final String[] index = {
		CommandHelp.header("Shop Help"),
		help.toIndexString(),
		pending.toIndexString()
	};
	public static final String[] indexSelected = { };
	public static final String[] indexAdmin = {
		create.toIndexString()
	};
	public static final String[] indexSelectedAdmin = {
		remove.toIndexString()
	};
	public static final String[] indexNotOwner = {
		buy.toIndexString(),
		sell.toIndexString()
	};
	public static final String[] indexOwner = {
		add.toIndexString(),
		restock.toIndexString(),
		set.toIndexString()
	};

	/**
	 * Shows generic shop help to a player.
	 * @param sender the player
	 */
	public static void showHelp(CommandSender sender) {
		sender.sendMessage(Help.index);
	}
	
	/**
	 * Shows context-sensitive help to a player based on that player's selection.
	 * @param sender the player
	 * @param selection the player's shop selection, or null if the player has no selection
	 */
	public static void showHelp(CommandSender sender, ShopSelection selection) {
		sender.sendMessage(Help.index);
		if (sender.hasPermission("shops.admin"))
			sender.sendMessage(Help.indexAdmin);
		if (selection != null) {
			sender.sendMessage(Help.indexSelected);
			if (sender.hasPermission("shops.admin"))
				sender.sendMessage(Help.indexSelectedAdmin);
			if (selection.isOwner)
				sender.sendMessage(Help.indexOwner);
			else
				sender.sendMessage(Help.indexNotOwner);
		}
	}

	private static final HashMap<String, CommandHelp> commands = new HashMap<String, CommandHelp>();
	
	/**
	 * Gets help for a specific action.
	 * @param action the action
	 * @return the action's help
	 */
	public static final CommandHelp getHelpFor(String action) {
		return commands.get(action);
	}
	
	static {
		commands.put("help", help);
		commands.put("h", help);
		commands.put("create", create);
		commands.put("c", create);
		commands.put("remove", remove);
		commands.put("rm", remove);
		commands.put("pending", pending);
		commands.put("p", pending);
		commands.put("buy", buy);
		commands.put("b", buy);
		commands.put("sell", sell);
		commands.put("s", sell);
		commands.put("add", add);
		commands.put("a", add);
		commands.put("restock", restock);
		commands.put("r", restock);
		commands.put("set", set);
	}

	private static final String[] concat(String[] a, String[] b) {
		String[] result = new String[a.length + b.length];
		for (int i = 0; i < a.length; ++i)
			result[i] = a[i];
		for (int i = 0; i < b.length; ++i)
			result[i + a.length] = b[i];
		return result;
	}
}
