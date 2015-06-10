package tbax.shops.notification;

import org.bukkit.entity.Player;

/**
 * A BuyNotification notifies a shop owner that someone bought an item
 * from him/her.
 */
public class DeathNotification implements Notification {
    private static final long serialVersionUID = 1L;
    
    public double tax;

    public DeathNotification(double tax) {
        this.tax = tax;
    }
    
    @Override
    public String getMessage(Player player) {
        return String.format("§FYou were fined §a$%.2f§F for dying.", tax);
    }

}
