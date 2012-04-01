package qs.swornshop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	
	private static final int SIGN = 63;
	
	public static Economy econ;

	public static HashMap<String, Long> aliases = new HashMap<String, Long>();
	public static HashMap<Long, String> itemNames = new HashMap<Long, String>();
	
	protected HashMap<Location, Shop> shops = new HashMap<Location, Shop>();
	protected HashMap<Player, ShopSelection> selectedShops = new HashMap<Player, ShopSelection>();
	protected Logger log;
	
	public Main() {}

	@Override
	public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
		log = this.getLogger();
		loadItemNames();
		loadAliases();
		if(!economySetup()){
			log.info("WARNING");
			log.info("Could not set up server economy!");
			log.info("This could be caused by Vault not being installed.");
			log.info("SwornShops may not funciton correctly");
		}
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
				Help.showHelp(pl, selection);
				return true;
			}
			String action = args[0];
			if (action.equalsIgnoreCase("create") || 
					action.equalsIgnoreCase("c")) {
				if (args.length < 2) {
					sendError(pl, Help.create.toUsageString());
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
				
			} else if (action.equalsIgnoreCase("add") ||
					action.equalsIgnoreCase("a")) {
				if (args.length < 2) {
					sendError(pl, Help.add.toUsageString());
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
					retailAmount = Math.round(100f * Float.parseFloat(args[1])) / 100f;
				} catch (NumberFormatException e) {
					sendError(pl, "Invalid buy price");
					sendError(pl, Help.add.toUsageString());
					return true;
				}
				try {
					refundAmount = args.length > 2 ? Math.round(100f * Float.parseFloat(args[2])) / 100f : -1;
				} catch (NumberFormatException e) {
					sendError(pl, "Invalid sell price");
					sendError(pl, Help.add.toUsageString());
					return true;
				}
				ItemStack stack = pl.getItemInHand();
				if (stack == null || stack.getTypeId() == 0) {
					sendError(pl, "You must be holding the item you wisth to add to this shop");
					return true;
				}
				
				if (selection.shop.containsItem(stack)) {
					sendError(pl, "That item has already been added to this shop");
					sendError(pl, "Use /shop restock to restock");
					return true;
				}
				ShopEntry newEntry = new ShopEntry();
				newEntry.setItem(stack);
				newEntry.retailPrice = retailAmount;
				newEntry.refundPrice = refundAmount;
				selection.shop.addEntry(newEntry);
				
				pl.setItemInHand(null);
				
			} else if ((action.equalsIgnoreCase("restock") ||
					action.equalsIgnoreCase("r"))) {
				if (selection == null) {
					sendError(pl, "You must select a shop");
					return true;
				}
				if (!selection.isOwner && !pl.hasPermission("shops.admin")) {
					sendError(pl, "You cannot restock this shop");
					return true;
				}
				
				ItemStack stack = pl.getItemInHand();
				if (stack == null || stack.getTypeId() == 0) {
					sendError(pl, "You must be holding the item you wish to add to this shop");
					return true;
				}
				ShopEntry entry = selection.shop.findEntry(stack);
				if (entry == null) {
					sendError(pl, "That item has not been added to this shop");
					sendError(pl, "Use /shop add to add a new item");
					return true;
				}
				entry.item.setAmount(entry.item.getAmount() + stack.getAmount());
				pl.setItemInHand(null);
				
			} else if (action.equalsIgnoreCase("set")) {
				if (!selection.isOwner && !pl.hasPermission("shop.admin")) {
					sendError(pl, "You cannot change this shop's prices");
					return true;
				}
				if (args.length < 3) {
					sendError(pl, Help.set.toUsageString());
					return true;
				}
				
				long item = getItemFromAlias(args[1]);
				int id = (int) (item >> 16);
				short damage = (short) (item & 0xFFFF);
				
				Shop shop = selection.shop;
				ShopEntry entry = shop.findEntry(id, damage);
				if (entry == null) {
					sendError(pl, "That item is not in this shop");
					return true;
				}
				
				float retailAmount, refundAmount;
				try {
					retailAmount = Math.round(100f * Float.parseFloat(args[1])) / 100f;
				} catch (NumberFormatException e) {
					sendError(pl, "Invalid buy price");
					sendError(pl, Help.set.toUsageString());
					return true;
				}
				try {
					refundAmount = args.length > 2 ? Math.round(100f * Float.parseFloat(args[2])) / 100f : -1;
				} catch (NumberFormatException e) {
					sendError(pl, "Invalid sell price");
					sendError(pl, Help.set.toUsageString());
					return true;
				}
				
				entry.retailPrice = retailAmount;
				entry.refundPrice = refundAmount;
				
			} else if (action.equalsIgnoreCase("buy") ||
					action.equalsIgnoreCase("b")) {
				if (args.length < 3) {
					sendError(pl, Help.buy.toUsageString());
					return true;
				}
				if (selection == null) {
					sendError(pl, "You must select a shop");
					return true;
				}
				if (selection.isOwner) {
					sendError(pl, "You cannot buy items from this shop");
					return true;
				}
				
				int amount;
				try {
					amount = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					sendError(pl, Help.buy.toUsageString());
					return true;
				}
				if (amount <= 0) {
					sendError(pl, "You must buy a positive number of this item");
					return true;
				}
				
				long item = getItemFromAlias(args[1]);
				int id = (int) (item >> 16);
				short damage = (short) (item & 0xFFFF);
				
				Shop shop = selection.shop;
				ShopEntry entry = shop.findEntry(id, damage);
				if (entry == null) {
					sendError(pl, "That item is not in this shop");
					return true;
				}
				if (entry.item.getAmount() < amount) {
					sendError(pl, "There are not enough of that item in the shop");
					return true;
				}
				int max = entry.item.getMaxStackSize();
				String itemName = getItemName(entry.item);
				if (max > -1 && amount > max) {
					sendError(pl, String.format("You may only buy §B%d %s§C at once", max, itemName));
					return true;
				}
				if(!(econ.has(pl.getName(), amount * entry.retailPrice))){
					sendError(pl, "You do not have sufficient funds");
					return true;
				}
				ItemStack purchased = entry.item.clone();
				purchased.setAmount(amount);
				
				HashMap<Integer, ItemStack> overflow =  pl.getInventory().addItem(purchased);
				int refunded = 0;
				if (overflow.size() > 0) {
					refunded = overflow.get(0).getAmount();
					if (overflow.size() == amount) {
						sendError(pl, "You do not have any room in your inventory");
						return true;
					}
					sender.sendMessage(String.format(
							"Only §B%d %s§F fit in your inventory. You were charged §B$%.2f§F.",
							amount - refunded, itemName, (amount - refunded) * entry.retailPrice));
				} else {
					sender.sendMessage(String.format(
							"You bought §B%d %s§F for §B$%.2f§F.",
							amount, itemName, amount * entry.retailPrice));
				}
				econ.withdrawPlayer(pl.getName(), (amount - refunded) * entry.retailPrice);
				entry.item.setAmount(entry.item.getAmount() - (amount - refunded));
				econ.depositPlayer(shop.owner, (amount - refunded) * entry.retailPrice);
				
			} else if (action.equalsIgnoreCase("lookup")) {
				if (args.length < 2) {
					sendError(pl, Help.lookup.toUsageString());
					return true;
				}
				Long alias = getItemFromAlias(args[1]);
				if (alias == null) {
					sendError(pl, "Alias not found");
					return true;
				}
				int id = (int) (alias >> 16);
				int damage = (int) (alias & 0xFFFF);
				sender.sendMessage(String.format("%s is an alias for %d:%d", args[1], id, damage));
				
			} else if ((action.equalsIgnoreCase("help") ||
					action.equalsIgnoreCase("h")) &&
					args.length > 1) {
				String helpCmd = args[1];
				CommandHelp h = Help.getHelpFor(helpCmd);
				if (h == null) {
					sendError(pl, String.format("'/shop %s' is not an action", helpCmd));
					return true;
				}
				pl.sendMessage(h.toHelpString());
				
			} else {
				Help.showHelp(pl, selection);
			}
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block b = event.getClickedBlock();
		if (b == null || b.getTypeId() != SIGN) return;
		
		Shop shop = shops.get(b.getLocation());
		if (shop == null) return;
		
		Player pl = event.getPlayer();
		
		boolean isOwner = shop.owner.equals(pl.getName());
		
		ShopSelection selection = selectedShops.get(pl);
		if (selection == null) {
			selection = new ShopSelection();
			selectedShops.put(pl, selection);
		}
		if (selection.shop == shop) {
			int pages = shop.getPages();
			if (pages == 0) {
				selection.page = 0;
			} else {
				int delta = event.getAction() == Action.LEFT_CLICK_BLOCK ? -1 : 1;
				selection.page = (((selection.page + delta) % pages) + pages) % pages;
			}
			pl.sendMessage("");
			pl.sendMessage("");
		} else {
			selection.isOwner = isOwner;
			selection.shop = shop;
			selection.page = 0;
			
			pl.sendMessage(new String[] {
				isOwner ? "§FWelcome to your shop." :
						String.format("§FWelcome to §B%s§F's shop.", shop.owner),
				"§7For help with shops, type §3/shop help§7."
			});
		}
		
		showListing(pl, selection);
		
		event.setCancelled(true);
		if (event.getAction() == Action.LEFT_CLICK_BLOCK)
			b.getState().update();
	}

	/**
	 * Attempts to find an item which matches the given item name (alias).
	 * @param alias the item name
	 * @return a Long which contains the item ID and damage value as follows: (id << 16) | (damage)
	 */
	public Long getItemFromAlias(String alias) {
		alias = alias.toLowerCase();
		return aliases.get(alias);
	}

	/**
	 * Informs a player of an error.
	 * @param sender the player
	 * @param message the error message
	 */
	protected void sendError(CommandSender sender, String message) {
		sender.sendMessage("§C" + message);
	}
	
	/**
	 * Show a page of a shop's inventory listing.
	 * @param sender the player to which the listing is shown
	 * @param selection the player's shop selection
	 */
	protected void showListing(CommandSender sender, ShopSelection selection) {
		Shop shop = selection.shop;
		int pages = shop.getPages();
		if (pages == 0) {
			sender.sendMessage(CommandHelp.header("Empty"));
			sender.sendMessage("");
			sender.sendMessage("This shop has no items");
			int stop = Shop.ITEMS_PER_PAGE - 2;
			if (selection.isOwner) {
				sender.sendMessage("Use /shop add to add items");
				--stop;
			}
			for (int i = 0; i < stop; ++i) {
				sender.sendMessage("");
			}
			return;
		}
		sender.sendMessage(CommandHelp.header(String.format("Page %d/%d", selection.page + 1, pages)));
		int i = selection.page * Shop.ITEMS_PER_PAGE,
			stop = (selection.page + 1) * Shop.ITEMS_PER_PAGE,
			max = Math.min(stop, shop.getInventorySize());
		for (; i < max; ++i)
			sender.sendMessage(shop.getEntryAt(i).toString(this));
		for (; i < stop; ++i)
			sender.sendMessage("");
	}
	
	/**
	 * Loads the alias map from the aliases.txt resource.
	 */
	public void loadAliases() {
		InputStream stream = getResource("aliases.txt");
		if (stream == null)
			return;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0 || line.charAt(0) == '#') continue;
				Scanner current = new Scanner(line);
				String name = current.next();
				int id = current.nextInt();
				int damage = current.hasNext() ? current.nextInt() : 0;
				aliases.put(name, (long) id << 16 | damage);
			}
			stream.close();
		} catch (IOException e) {
			log.warning("Failed to load aliases: " + e.toString());
		}
	}
	
	/**
	 * Loads the item names map from the items.txt resource.
	 */
	public void loadItemNames() {
		InputStream stream = getResource("items.txt");
		if (stream == null)
			return;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			String line = br.readLine();
			while (line != null) {
				if (line.length() == 0 || line.charAt(0) == '#') continue;
				Scanner current = new Scanner(line);
				int id = current.nextInt(),
					damage = 0;
				String name = "";
				while (current.hasNext()) {
					name += ' ' + current.next();
				}
				if (name.length() == 0) {
					log.info(String.format("%s: %s", line, name));
					break;
				}
				itemNames.put((long) id << 16 | damage, name.substring(1));
				line = br.readLine();
				if (line != null && line.charAt(0) == '|') {
					do {
						if (line.length() == 0 || line.charAt(0) == '#') continue;
						current = new Scanner(line);
						if (!current.next().equals("|")) break;
						if (!current.hasNextInt(16)) break;
						damage = current.nextInt(16);
						name = "";
						while (current.hasNext()) {
							name += ' ' + current.next();
						}
						itemNames.put((long) id << 16 | damage, name.substring(1));
					} while ((line = br.readLine()) != null);
				}
			}
			stream.close();
		} catch (IOException e) {
			log.warning("Failed to load item names: " + e.toString());
		}
	}

	/**
	 * Gets the name of an item.
	 * @param item an item stack
	 * @return the item's name
	 */
	public String getItemName(ItemStack item) {
		return getItemName(item.getTypeId(), item.getDurability());
	}

	/**
	 * Gets the name of an item.
	 * @param id the item's id
	 * @param damage the item's damage value (durability)
	 * @return the item's name
	 */
	public String getItemName(int id, int damage) {
		String name = itemNames.get((long) id << 16 | damage);
		if (name == null) {
			name = itemNames.get((long) id << 16);
			if (name == null) return String.format("%d:%d", id, damage);
		}
		return name;
	}
	
	/**
	 * Sets up Vault.
	 * @return true on success, false otherwise
	 */
	private boolean economySetup() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
}
