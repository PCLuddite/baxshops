package qs.shops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import qs.shops.notification.BuyNotification;
import qs.shops.notification.Claimable;
import qs.shops.notification.LollipopNotification;
import qs.shops.notification.Notification;
import qs.shops.notification.Request;
import qs.shops.notification.SellRequest;
import qs.shops.serialization.BlockLocation;
import qs.shops.serialization.State;

public class Main extends JavaPlugin implements Listener {
	
	// TODO: Timed notifications
	
	/**
	 * The block ID for a signpost
	 */
	private static final int SIGN = 63;
	
	/**
	 * The distance from the sign in any direction which the player can go before they leave the shop
	 */
	private static final int SHOP_RANGE = 4*4;
	
	/**
	 * A single instance of Main for external access
	 */
	public static Main instance;
	
	/**
	 * The Vault economy
	 */
	public static Economy econ;

	/**
	 * A lookup table for aliases.
	 * Aliases are stored as <code>alias =&gt; (ID &lt;&lt; 16) | (damageValue)</code>
	 */
	protected static HashMap<String, Long> aliases = new HashMap<String, Long>();
	/**
	 * A lookup table for item names.
	 * Item names are stored as <code>(ID &lt;&lt; 16) | (damageValue) =&gt; itemName</code>
	 */
	protected static HashMap<Long, String> itemNames = new HashMap<Long, String>();
	
	/**
	 * A map of shops, accessed by their location in the world
	 */
	protected HashMap<Location, Shop> shops = new HashMap<Location, Shop>();
	/**
	 * A map containing each player's currently selected shop and other selection data
	 */
	protected HashMap<Player, ShopSelection> selectedShops = new HashMap<Player, ShopSelection>();
	/**
	 * A map containing each player's notifications
	 */
	protected HashMap<String, ArrayDeque<Notification>> pending = new HashMap<String, ArrayDeque<Notification>>();
	/**
	 * The plugin logger
	 */
	protected Logger log;
	
	public Main() {}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		instance = this;
		log = this.getLogger();
		loadItemNames();
		loadAliases();
		if (!economySetup()) {
			log.warning("Could not set up server economy! Is Vault installed?");
			getPluginLoader().disablePlugin(this);
			return;
		}
		State state = reloadAll();
		if (state != null) {
			shops = state.getShops();
			pending = state.pending;
		} else{
			log.info("Shops could not be loaded. If this is the first launch of the plugin, " +
					"this is expected. If not, your data files may be corrupt. Try replacing " +
					"state.dat with one of the .dat files in the backups folder)");
		}

		// run an initial save 5 minutes after starting, then a recurring save
		// every 30 minutes after the first save
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				saveAll();
			}
		}, 6000L, 36000L);
	}
	
	@Override
	public void onDisable() {
		saveAll();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		return false;
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
	 * Gets the name of an item.
	 * @param item an item stack
	 * @return the item's name
	 */
	public String getItemName(ItemStack item) {
		return null;
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
	 * Gets a list of notifications for a player.
	 * @param pl the player
	 * @return the player's notifications
	 */
	public ArrayDeque<Notification> getNotifications(Player pl) {
		return getNotifications(pl.getName());
	}
	
	/**
	 * Gets a list of notifications for a player.
	 * @param player the player
	 * @return the player's notifications
	 */
	public ArrayDeque<Notification> getNotifications(String player) {
		ArrayDeque<Notification> n = pending.get(player);
		if (n == null) {
			n = new ArrayDeque<Notification>();
			pending.put(player, n);
		}
		return n;
	}
	
	/**
	 * Shows a player his/her most recent notification.
	 * Also shows the notification count.
	 * @param pl the player
	 */
	public void showNotification(Player pl) {
		showNotification(pl, true);
	}

	/**
	 * Shows a player his/her most recent notification.
	 * @param pl the player
	 * @param showCount whether the notification count should be shown as well
	 */
	public void showNotification(Player pl, boolean showCount) {
		ArrayDeque<Notification> notifications = getNotifications(pl);
		if (notifications.isEmpty()) {
			if (showCount)
				pl.sendMessage("§7You have no notifications");
			return;
		}
		if (showCount) {
			int size = notifications.size();
			pl.sendMessage(size == 1 ? "§7You have §31§7 notification" : String.format("§7You have §3%d§7 notifications", size));
		}
		
		Notification n = notifications.getFirst();
		pl.sendMessage(n.getMessage(pl));
		if (n instanceof Request)
			pl.sendMessage("§7Use §3/shop accept§7 or §3/shop reject§7 to manage this request");
		else if (n instanceof Claimable)
			pl.sendMessage("§7Use §3/shop claim§7 to claim and remove this notification");
		else notifications.removeFirst();
	}
	/**
	 * Sends a notification to a player.
	 * @param pl the player
	 * @param n the notification
	 */
	public void sendNotification(Player pl, Notification n) {
		sendNotification(pl.getName(), n);
	}

	/**
	 * Sends a notification to a player.
	 * @param player the player
	 * @param n the notification
	 */
	public void sendNotification(String player, Notification n) {
		ArrayDeque<Notification> ns = getNotifications(player);
		ns.add(n);
		Player pl = getServer().getPlayer(player);
		if (pl != null && pl.isOnline())
			showNotification(pl, false);
	}
	
	/**
	 * Checks whether an item stack will fit in a player's inventory.
	 * @param pl the player
	 * @param item the item
	 * @return whether the item will fit
	 */
	public static boolean inventoryFitsItem(Player pl, ItemStack item) {
		return false;
	}

	/**
	 * Informs a player of an error.
	 * @param sender the player
	 * @param message the error message
	 */
	public static void sendError(CommandSender sender, String message) {
		sender.sendMessage("§C" + message);
	}

	/**
	 * Show a page of a shop's inventory listing.
	 * @param sender the player to which the listing is shown
	 * @param selection the player's shop selection
	 */
	public static void showListing(CommandSender sender, ShopSelection selection) {
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
	}

	/**
	 * Loads the alias map from the aliases.txt resource.
	 */
	public void loadAliases() {
		InputStream stream = getResource("aliases.txt");
		if (stream == null)
			return;
		int i = 1;
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
				i++;
			}
			stream.close();
		} catch (IOException e) {
			log.warning("Failed to load aliases: " + e.toString());
		}catch (NoSuchElementException e){
			log.info("loadAliases broke at line: " + i);
			e.printStackTrace();
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
				if (name.length() == 0)
					break;
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
	 * Sets up Vault.
	 * @return true on success, false otherwise
	 */
	private boolean economySetup() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
		
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) return false;
		
		econ = rsp.getProvider();
		return econ != null;
	}
	
	/**
	 * Saves all shops
	 */
	public void saveAll() {
		if (!backup())
			log.warning("Failed to back up shops");
		
		State state = new State();
		for (Entry<Location, Shop> entry : shops.entrySet()) {
			Shop shop = entry.getValue();
			state.shops.put(new BlockLocation(entry.getKey()), shop);
		}
		state.pending = this.pending;
		
		try {
			File dir = getDataFolder();
			if (!dir.exists()) dir.mkdirs();
			File f = new File(dir, "shops.dat");
			FileOutputStream fs = new FileOutputStream(f);
			ObjectOutputStream out = new ObjectOutputStream(fs);
			out.writeObject(state);
		} catch (FileNotFoundException e) {
			log.warning("Save failed");
			e.printStackTrace();
		} catch (IOException e) {
			log.warning("Save failed");
			e.printStackTrace();
		}
	}
	
	/**
	 * Attempts to back up the shops.dat savefile.
	 * @return a boolean indicating success
	 */
	public boolean backup() {
		File stateLocation = new File(getDataFolder(), "shops.dat");
		if (stateLocation.exists()) {
			long timestamp = new Date().getTime();
			File backupFolder = new File(getDataFolder(), "backups");
			if (!backupFolder.exists()) backupFolder.mkdirs();
			
			File[] backups = backupFolder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File f, String name) {
					return name.endsWith(".dat");
				}
			});
			int b = getConfig().getInt("Backups");
			if (b > 0 && backups.length >= b) {
				File delete = null;
				long oldest = Long.MAX_VALUE;
				for (File f : backups) {
					String name = f.getName();
					int i = name.indexOf('.');
					try {
						long compare = Long.parseLong(name.substring(0, i));
						if (compare < oldest) {
							oldest = compare;
							delete = f;
						}
					} catch (NumberFormatException e) { }
				}
				if (delete != null) delete.delete();
			}
			
			try {
				File backup = new File(getDataFolder(), String.format("backups/%d.dat", timestamp));
				InputStream in = new FileInputStream(stateLocation);
				OutputStream out = new FileOutputStream(backup);
				byte[] buf = new byte[1024];
				int i;
				while ((i = in.read(buf)) > 0)
					out.write(buf, 0, i);
				in.close();
				out.close();
			} catch (FileNotFoundException e) {
				log.warning("Backup failed");
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				log.warning("Backup failed");
				e.printStackTrace();
				return false;
			}
			return true;
		}
		log.warning("Aborting backup: shops.dat not found");
		return false;
	}
	
	
	/**
	 * Loads all shops from the shops.dat savefile.
	 * @return the saved State
	 */
	public State reloadAll() {
		File stateLocation = new File(getDataFolder(), "shops.dat");
		if (stateLocation.exists()) {
			try {
				FileInputStream fs = new FileInputStream(stateLocation);
				ObjectInputStream stream = new ObjectInputStream(fs);
				Object obj = stream.readObject();
				if (obj instanceof State)
					return (State) obj;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
}
