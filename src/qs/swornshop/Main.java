package qs.swornshop;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	private static final int SIGN = 63;
	
	public static final HashMap<String, CommandHelp> help = new HashMap<String, CommandHelp>();
	
	public static final CommandHelp cmdHelp = new CommandHelp("shop help", "h", "[action]", "show help with shops",
			CommandHelp.arg("action", "get help on a /shop action, e.g. /shop h create"));
	public static final CommandHelp cmdCreate = new CommandHelp("shop create", "c", "<owner>", "create a new shop", 
			CommandHelp.arg("owner", "the owner of the shop"));
	public static final CommandHelp cmdRemove = new CommandHelp("shop remove", "rm", null, "removes this shop");
	
	public static final CommandHelp cmdPending = new CommandHelp("shop pending", "p", null, "view pending shop requests", 
			"Shows a list of pending offers to sell items to your shops",
			"Use /shop accept and /shop reject on these offers.");
	
	public static final CommandHelp cmdBuy = new CommandHelp("shop buy", "b", "<item> <quantity>", "buy an item from this shop", 
			CommandHelp.args(
				"item", "the name of the item",
				"quantity", "the quantity you wish to buy"
			));
	public static final CommandHelp cmdSell = new CommandHelp("shop sell", "s", "<item> <quantity> [price=auto]", "request to sell an item to this shop",
			CommandHelp.args(
				"item", "the name of the item",
				"quantity", "the quantity you wish to sell",
				"price", "the price (for the entire quantity); defaults to the store's price times the quantity"
			));
	
	public static final CommandHelp cmdAdd = new CommandHelp("shop add", "a", "<buy-price> [sell-price=none]", "add your held item to this shop",
			CommandHelp.args(
				"buy-price", "the price of a single item in the stack",
				"sell-price", "the selling price of a single item in the stack (by default the item cannot be sold)"
			));
	
	static {
		help.put("help", cmdHelp);
		help.put("h", cmdHelp);
		help.put("create", cmdCreate);
		help.put("c", cmdCreate);
		help.put("remove", cmdRemove);
		help.put("rm", cmdRemove);
		help.put("pending", cmdPending);
		help.put("p", cmdPending);
		help.put("buy", cmdBuy);
		help.put("b", cmdBuy);
		help.put("sell", cmdSell);
		help.put("s", cmdSell);
		help.put("add", cmdAdd);
		help.put("a", cmdAdd);
	}
	
	public static final String[] shopHelp = {
		CommandHelp.header("Shop Help"),
		cmdHelp.toIndexString(),
		cmdPending.toIndexString()
	};
	public static final String[] shopSelectedHelp = { };
	public static final String[] shopAdminHelp = {
		cmdCreate.toIndexString()
	};
	public static final String[] shopSelectedAdminHelp = {
		cmdRemove.toIndexString()
	};
	public static final String[] shopNotOwnerHelp = {
		cmdBuy.toIndexString(),
		cmdSell.toIndexString()
	};
	public static final String[] shopOwnerHelp = {
		cmdAdd.toIndexString()
	};
	public static HashMap<String, Long> aliases = new HashMap<String, Long>();
	
	protected HashMap<Location, Shop> shops = new HashMap<Location, Shop>();
	protected HashMap<Player, ShopSelection> selectedShops = new HashMap<Player, ShopSelection>();
	protected Logger log;
	
	public Main() {}

	@Override
	public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
		log = this.getLogger();
		aliases = parseItems();
		System.out.println(aliases.get("wood"));
	}
	@Override
	public void onDisable() {}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (command.getName().equalsIgnoreCase("shop")) {
			if (!(sender instanceof Player)) {
				sendError(sender, "/shop can only be used by a player");
				return true;
			}
			Player pl = (Player) sender;
			ShopSelection selection = selectedShops.get(pl);
			if (args.length == 0) {
				showHelp(pl, selection);
				return true;
			}
			String action = args[0];
			if (action.equalsIgnoreCase("create")  || 
					action.equalsIgnoreCase("c")) {
				if (args.length < 2) {
					sendError(pl, cmdCreate.toUsageString());
					return true;
				}
				if (!sender.hasPermission("shops.admin")) {
					sendError(pl, "You cannot create shops");
					return true;
				}
				Location loc = pl.getLocation();
				Block b = loc.getBlock();
				byte angle = (byte) ((((int) loc.getYaw() + 225) / 90) << 2);
				b.setTypeIdAndData(SIGN, angle, false);

				Sign sign = (Sign) b.getState();
				String owner = args[1];
				sign.setLine(1, (owner.length() < 13 ? owner : owner.substring(0, 12) + '…') + "'s");
				sign.setLine(2, "shop");
				sign.update();

				Shop shop = new Shop();
				shop.owner = owner;
				shop.location = b.getLocation();
				shops.put(shop.location, shop);
				
			} else if (action.equalsIgnoreCase("remove") || 
					action.equalsIgnoreCase("rm")) {
				if (selection == null) {
					sendError(pl, "You must select a shop");
					return true;
				}
				if (!pl.hasPermission("shop.admin") && !selection.isOwner) {
					sendError(pl, "You cannot remove this shop");
					return true;
				}
				Location loc = selection.shop.location;
				Block b = loc.getBlock();
				Sign sign = (Sign) b.getState();
				sign.setLine(0, "This shop is");
				sign.setLine(1, "out of");
				sign.setLine(2, "business.");
				sign.setLine(3, "Sorry! D:");
				sign.update();
				shops.remove(loc);
				
				pl.sendMessage("§B" + selection.shop.owner + "§F's shop has been removed");
				
			} else if ((action.equalsIgnoreCase("add") ||
					action.equalsIgnoreCase("a"))) {
				if (args.length < 2) {
					sendError(pl, cmdAdd.toUsageString());
					return true;
				}
				if (selection == null) {
					sendError(pl, "You must select a shop");
					return true;
				}
				if (!selection.isOwner && !pl.hasPermission("shops.admin")) {
					sendError(pl, "You cannot add items to this shop");
					return true;
				}
				
				float retailAmount, refundAmount;
				try {
					retailAmount = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					sendError(pl, "Invalid buy price");
					sendError(pl, cmdAdd.toUsageString());
					return true;
				}
				try {
					refundAmount = args.length > 2 ? Integer.parseInt(args[2]) : -1;
				} catch (NumberFormatException e) {
					sendError(pl, "Invalid sell price");
					sendError(pl, cmdAdd.toUsageString());
					return true;
				}
				ItemStack stack = pl.getItemInHand().clone();
				ShopEntry newEntry = new ShopEntry();
				newEntry.retailPrice = retailAmount;
				newEntry.refundPrice = refundAmount;
				selection.shop.inventory.put(stack, newEntry);
				pl.getItemInHand().setAmount(0);
				
			}
			else if((action.equalsIgnoreCase("testlookup"))){
				if(args.length == 2){
					Long alias = aliases.get(args[1]);
					if(alias != null){
						
						int id = (int) (alias >> 16);
						int damage = (int) (alias & 0xFFFF);
						sender.sendMessage(args[1] + " is an alias for: " + id + ":" + damage);
					}
					else{
						sender.sendMessage("that is not a valid alias!");
					}
				}
				else{
					sender.sendMessage("Invalid arguments");
				}
			}
			else if ((action.equalsIgnoreCase("help") ||
					action.equalsIgnoreCase("h")) &&
					args.length > 1) {
				String helpCmd = args[1];
				CommandHelp h = help.get(helpCmd);
				if (h == null) {
					sendError(pl, String.format("'/shop %s' is not an action", helpCmd));
					return true;
				}
				pl.sendMessage(h.toHelpString());
				
			} else {
				showHelp(pl, selection);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Shows generic shop help to a player
	 * @param sender the player
	 */
	protected void showHelp(CommandSender sender) {
		sender.sendMessage(shopHelp);
	}
	/**
	 * Shows context-sensitive help to a player based on that player's selection 
	 * @param sender the player
	 * @param selection the player's shop selection, or null if the player has no selection
	 */
	protected void showHelp(CommandSender sender, ShopSelection selection) {
		sender.sendMessage(shopHelp);
		if (sender.hasPermission("shops.admin"))
			sender.sendMessage(shopAdminHelp);
		if (selection != null) {
			sender.sendMessage(shopSelectedHelp);
			if (sender.hasPermission("shops.admin"))
				sender.sendMessage(shopSelectedAdminHelp);
			if (selection.isOwner)
				sender.sendMessage(shopOwnerHelp);
			else
				sender.sendMessage(shopNotOwnerHelp);
		}
	}

	/**
	 * Informs a player of an error
	 * @param sender the player
	 * @param message the error message
	 */
	protected void sendError(CommandSender sender, String message) {
		sender.sendMessage("§C" + message);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block b = event.getClickedBlock();
		if (b != null && b.getTypeId() == SIGN) {
			Shop shop = shops.get(b.getLocation());
			if (shop != null) {
				Player pl = event.getPlayer();
				
				boolean isOwner = shop.owner.equals(pl.getName());
				
				ShopSelection selection = selectedShops.get(pl);
				if (selection == null) selection = new ShopSelection();
				selection.isOwner = isOwner;
				selection.shop = shop;
				selection.page = 0;
				selectedShops.put(pl, selection);
				
				pl.sendMessage(new String[] {
					isOwner ? "§FWelcome to your shop." :
							String.format("§FWelcome to §B%s§F's shop.", shop.owner),
					"§7For help with shops, type §3/shop help§7."
				});
				
				event.setCancelled(true);
				if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
					b.getState().update();
				}
			}
		}
	}
	
	public HashMap<String, Long> parseItems(){
		File f = new File(this.getDataFolder(), "aliases.txt");
		FileInputStream fstream;
		HashMap<String, Long> itemNames = new HashMap<String, Long>();
		try {
			fstream = new FileInputStream(f);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine; 
			while ((strLine = br.readLine()) != null){
				if(strLine.charAt(0) == '#'){continue;}		
				Scanner current = new Scanner(strLine);
				String name = current.next();
				int typeId = current.nextInt();
				int damageValue = 0;
				long output;
				if(!current.hasNext()){
					output = typeId << 16 | damageValue;
					itemNames.put(name, output);
					continue;
				}
				damageValue = current.nextInt();
				 output = typeId << 16 | damageValue;
				itemNames.put(name, output);
				
			}
			in.close();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return itemNames;
	}
	
}
