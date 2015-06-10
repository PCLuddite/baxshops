package tbax.shops;

import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class Main {

    /**
     * A single instance of Main for external access
     */
    public static Main instance;

    /**
     * The file and text resources for the plugin
     */
    public Resources res;
    
    /**
     * The Vault economy
     */
    public static Economy econ;
    
    public Logger log;
    
    public Main() {
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    public void onPlayerInteract(PlayerInteractEvent event) { 
    }

    public void onExplosion(EntityExplodeEvent event) {
    }

    public void onPlayerJoin(PlayerJoinEvent event) {
    }

    public void onPlayerMove(PlayerMoveEvent event) {
    }
    
    public void onPlayerDeath(PlayerDeathEvent event) {
    }
        
    /**
     * Informs a player of an error.
     *
     * @param sender the player
     * @param message the error message
     */
    public static void sendError(CommandSender sender, String message) {
        
    }
}
