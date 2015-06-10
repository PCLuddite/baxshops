/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tbax.shops.notification;

import org.bukkit.entity.Player;

/**
 *
 * @author Timothy
 */
public class GeneralNotification implements Notification {
    
    public String message;
    
    public GeneralNotification(String msg) {
        message = msg;
    }
    
    public String getMessage(Player player) {
        return message;
    }
}
