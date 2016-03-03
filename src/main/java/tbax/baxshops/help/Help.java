/* 
 * The MIT License
 *
 * Copyright © 2013-2015 Timothy Baxendale (pcluddite@hotmail.com) and 
 * Copyright © 2012 Nathan Dinsmore and Sam Lazarus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package tbax.baxshops.help;

import java.util.HashMap;
import org.bukkit.command.CommandSender;
import tbax.baxshops.ShopSelection;

/**
 * Help stores all help-related information, such as help for each command and
 * context-sensitive index topics.
 */
public final class Help
{
    public static final CommandHelp HELP = new CommandHelp("shop help", "h", "[action]", "show help with shops",
            CommandHelp.arg("action", "get help on a /shop action, e.g. /shop h create"));
    public static final CommandHelp CREATE = new CommandHelp("shop create", "mk", "<owner> [inf=no]", "create a new shop",
            CommandHelp.args(
                    "owner", "the owner of the shop",
                    "inf", "whether the shop is infinite"));
    public static final CommandHelp CREATE_OWNER = new CommandHelp("shop create", "mk", null, "create a new shop");
    public static final CommandHelp DELETE = new CommandHelp("shop delete", "del", null, "removes this shop");
    public static final CommandHelp SAVE = new CommandHelp("shop save", null, null, "saves all shops");
    public static final CommandHelp BACKUP = new CommandHelp("shop backup", null, null, "backs up shops");

    public static final CommandHelp NOTIFICATIONS = new CommandHelp("shop notifications", "n,pending,p", null, "view notifications",
            "Shows a list of notifications to sell items to your shops",
            "These can be offers (e.g., someone wishes to sell you an item)",
            "or messages (e.g., an offer was accepted).",
            "Use /shop accept and /shop reject on offers.");

    public static final CommandHelp LOLLIPOP = new CommandHelp("shop lollipop", "lol", "[player [tastiness]]", CommandHelp.args(
            "player", "the player",
            "tastiness", "the tastiness (0-100)"
    ));

    public static final CommandHelp ACCEPT = new CommandHelp("shop accept", "yes,a", null, "accept your most recent notification");
    public static final CommandHelp CLAIM = new CommandHelp("shop claim", "c", null, "claim your most recent notification");
    public static final CommandHelp REJECT = new CommandHelp("shop reject", "no", null, "reject your most recent notification");
    public static final CommandHelp SKIP = new CommandHelp("shop skip", "sk", null, "skip your most recent notification",
            "Moves your most recent notification to the end of the list");

    public static final CommandHelp BUY = new CommandHelp("shop buy", "b", "[item] <amount>", "buy an item from this shop",
            CommandHelp.args(
                    "item", "the name of the item or an entry number in the shop.    §LNote:§R enchanted items must be bought with an entry number",
                    "quantity", "the quantity you wish to buy"
            ));
    public static final CommandHelp SELL = new CommandHelp("shop sell", "s", "<item> <quantity> [price=auto]", "request to sell an item to this shop",
            CommandHelp.args(
                    "item", "the name of the item",
                    "quantity", "the quantity you wish to sell",
                    "price", "the price (for the entire quantity); defaults to the store's price times the quantity"
            ));

    public static final CommandHelp ADD = new CommandHelp("shop add", "+,ad", "<$buy> [$sell=no]", "add held item to this shop",
            CommandHelp.args(
                            "buy-price", "the price of a single item in the stack",
                            "sell-price", "the selling price of a single item in the stack (by default the item cannot be sold)"
                    ));
    public static final CommandHelp REMOVE = new CommandHelp("shop remove", "rm", "<item>", "removes an item",
            CommandHelp.arg("item", "the name or entry number of the item to remove"));
    public static final CommandHelp RESTOCK = new CommandHelp("shop restock", "r", null, "restock this shop with your held item");
    public static final CommandHelp RESTOCKALL = new CommandHelp("shop restockall", null, null,
            "restocks using all items of the same type");
    public static final CommandHelp SET = new CommandHelp("shop set", null, "<item> <$buy> <$sell>", "change an item's price",
            CommandHelp.args(
                    "item", "the ID or name of the item to modify",
                    "buy-price", "the new price of a single item in the stack",
                    "sell-price", "the selling price of a single item in the stack (by default the item cannot be sold)"
            ));
    public static final CommandHelp SIGN = new CommandHelp("shop sign", null, "<line1>|<line2>…", "changes a shop's sign",
            CommandHelp.arg("text", "the new text of the sign, separated by |'s"));

    public static final CommandHelp LOOKUP = new CommandHelp("shop lookup", null, "<item-name>", "look up an item's ID and damage value",
            CommandHelp.arg("item-name", "the name of an alias for an item"));

    public static final CommandHelp FLAG = new CommandHelp("shop flag", null, "<name|list> [setting]", "Set a specific flag or list all flags applied to a selected shop",
            CommandHelp.args("name", "the name of the flag to set",
                    "setting", "the option to set the flag",
                    "list", "lists all flags applied to the shop"));
    
    public static final CommandHelp COPY = new CommandHelp("shop copy", null, null, "Copies the shop using a sign from the player's inventory");
        
    public static final CommandHelp TAKE = new CommandHelp("shop take", null, "<name> [amount]", "Takes an item from the shop",
            CommandHelp.args("name", "the name of the item",
                             "amount", "the amount to take. Default is 1."));
    
    public static final CommandHelp INFO = new CommandHelp("shop info", null, "<index>", "Gets extended information about an entry in a shop",
            CommandHelp.args("index", "the name or shop index of the entry"));
    
    public static final CommandHelp LIST = new CommandHelp("shop list", null, "", "List all shop locations");
    
    public static final CommandHelp TELEPORT = new CommandHelp("shop teleport", "tp", "<location>", "Pastes a shop from the clipboard",
            CommandHelp.args("location", "the location index (given by /shop list) to teleport to"));
    
    public static final CommandHelp SETANGLE = new CommandHelp("shop setface", "setangle", "<direction>", "Rotates the sign to face another direction",
            CommandHelp.args("direction", "\"North\", \"South\", \"East\", or \"West\""));
    
    public static final CommandHelp SETAMNT = new CommandHelp("shop setamnt", null, "<name> <amount>", "Sets the amount of an item is in the shop",
            CommandHelp.args("name", "the name or index of the item",
                             "amount", "the amount to stock"));
    
    public static final CommandHelp SETINDEX = new CommandHelp("shop setindex", null, "<old index> <new index>", "Changes the order of entries in a shop",
            CommandHelp.args("old index", "the original index for the entry",
                             "new index", "the new index for the entry"));

    /**
     * Shows generic shop help to a player.
     *
     * @param sender the player
     */
    public static void showHelp(CommandSender sender)
    {
        sender.sendMessage(HelpIndex.GENERAL);
    }

    /**
     * Shows context-sensitive help to a player based on that player's
     * selection.
     *
     * @param sender the player
     * @param selection the player's shop selection, or null if the player has
     * no selection
     */
    public static void showHelp(CommandSender sender, ShopSelection selection)
    {
        sender.sendMessage(HelpIndex.GENERAL);
        if (sender.hasPermission("shops.admin")) {
            sender.sendMessage(HelpIndex.ADMIN);
        }
        if (selection != null) {
            if (sender.hasPermission("shops.admin")) {
                sender.sendMessage(HelpIndex.SELECTED_ADMIN);
            }
            if (selection.isOwner) {
                sender.sendMessage(HelpIndex.OWNER);
            }
            else {
                sender.sendMessage(HelpIndex.NOT_OWNER);
            }
            sender.sendMessage(HelpIndex.SELECTED);
        }
    }

    private static final HashMap<String, CommandHelp> ALL_COMMANDS = new HashMap<>();

    /**
     * Gets help for a specific action.
     *
     * @param action the action
     * @return the action's help
     */
    public static final CommandHelp getHelpFor(String action)
    {
        return ALL_COMMANDS.get(action);
    }

    static
    {
        ALL_COMMANDS.put("help", HELP);
        ALL_COMMANDS.put("notifications", NOTIFICATIONS);
        ALL_COMMANDS.put("pending", NOTIFICATIONS);
        ALL_COMMANDS.put("accept", ACCEPT);
        ALL_COMMANDS.put("reject", REJECT);
        ALL_COMMANDS.put("claim", CLAIM);
        ALL_COMMANDS.put("skip", SKIP);

        ALL_COMMANDS.put("buy", BUY);
        ALL_COMMANDS.put("sell", SELL);

        ALL_COMMANDS.put("add", ADD);
        ALL_COMMANDS.put("remove", REMOVE);
        ALL_COMMANDS.put("restock", RESTOCK);
        ALL_COMMANDS.put("restockall", RESTOCKALL);
        ALL_COMMANDS.put("set", SET);
        ALL_COMMANDS.put("sign", SIGN);
        ALL_COMMANDS.put("setface", SETANGLE);
        ALL_COMMANDS.put("setangle", SETANGLE);
        ALL_COMMANDS.put("setindex", SETINDEX);

        ALL_COMMANDS.put("create", CREATE);
        ALL_COMMANDS.put("delete", DELETE);
        ALL_COMMANDS.put("save", SAVE);
        ALL_COMMANDS.put("backup", BACKUP);

        ALL_COMMANDS.put("h", HELP);
        ALL_COMMANDS.put("n", NOTIFICATIONS);
        ALL_COMMANDS.put("p", NOTIFICATIONS);
        ALL_COMMANDS.put("a", ACCEPT);
        ALL_COMMANDS.put("yes", ACCEPT);
        ALL_COMMANDS.put("no", REJECT);
        ALL_COMMANDS.put("c", CLAIM);
        ALL_COMMANDS.put("sk", SKIP);

        ALL_COMMANDS.put("b", BUY);
        ALL_COMMANDS.put("s", SELL);

        ALL_COMMANDS.put("ad", ADD);
        ALL_COMMANDS.put("rm", REMOVE);
        ALL_COMMANDS.put("+", ADD);
        ALL_COMMANDS.put("r", RESTOCK);

        ALL_COMMANDS.put("c", CREATE);
        ALL_COMMANDS.put("mk", CREATE);
        ALL_COMMANDS.put("del", DELETE);

        ALL_COMMANDS.put("flag", FLAG);
        ALL_COMMANDS.put("opt", FLAG);
        ALL_COMMANDS.put("option", FLAG);

        ALL_COMMANDS.put("copy", COPY);
        ALL_COMMANDS.put("list", LIST);
        
        ALL_COMMANDS.put("take", TAKE);
        ALL_COMMANDS.put("t", TAKE);
        
        ALL_COMMANDS.put("tp", TELEPORT);
        ALL_COMMANDS.put("teleport", TELEPORT);
        
        ALL_COMMANDS.put("info", INFO);
    }
}
