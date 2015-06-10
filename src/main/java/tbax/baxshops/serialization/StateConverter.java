/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tbax.baxshops.serialization;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Location;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Main;
import tbax.baxshops.notification.BuyClaim;
import tbax.baxshops.notification.BuyNotification;
import tbax.baxshops.notification.BuyRejection;
import tbax.baxshops.notification.BuyRequest;
import tbax.baxshops.notification.DeathNotification;
import tbax.baxshops.notification.GeneralNotification;
import tbax.baxshops.notification.LollipopNotification;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.notification.SaleNotification;
import tbax.baxshops.notification.SaleNotificationAuto;
import tbax.baxshops.notification.SaleRejection;
import tbax.baxshops.notification.SellRequest;
import tbax.shops.serialization.State2;

/**
 *
 * @author tbaxendale
 */
public class StateConverter {
    
    private final Logger log;
    private final Main main;
    private final StateFile state;
    
    public StateConverter(Main main, StateFile state) {
        log = main.getLogger();
        this.main = main;
        this.state = state;
    }
    
    public void Convert(State2 oldState) {
        HashMap<Location, tbax.shops.BaxShop> oldShops = oldState.getShops();
        HashMap<tbax.shops.Shop, Integer> oldUids = new HashMap<>();
        log.info("Converting old shop listings...");
        int current = 0; // assign uids
        for(Map.Entry<Location, tbax.shops.BaxShop> entry : oldShops.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                log.warning("Null entry in shop listing. Skipped.");
                continue;
            }
            tbax.shops.BaxShop oldShop = entry.getValue();
            if (oldShop.location == null) {
                log.warning("Null shop location. Skipped.");
                continue;
            }
            BaxShop newShop;
            try {
                newShop = new BaxShop(current, oldShop);
            }
            catch(NullPointerException e) {
                log.warning("NullPointerException. Skipped.");
                continue;
            }
            for(Location loc : newShop.getLocations()) {
                state.locations.put(loc, current);
            }
            state.shops.put(current, newShop);
            oldUids.put(entry.getValue(), current);
            current++;
        }
        log.info("Converting pending notifications...");
        for(Map.Entry<String, ArrayDeque<tbax.shops.notification.Notification>> entry : oldState.pending.entrySet()) {
            if (state.pending.get(entry.getKey()) == null) {
                state.pending.put(entry.getKey(), new ArrayDeque<Notification>());
            }
            for(tbax.shops.notification.Notification oldNote : entry.getValue()) {
                try {
                    Notification note = convertFromOld(oldNote, oldUids);
                    if (note == null) {
                        log.warning("Unknown notification type. Cannot convert.");
                    }
                    else {
                        state.pending.get(entry.getKey()).push(note);
                    }
                }
                catch(NullPointerException e) {
                    log.warning("NullPointerException. Skipped.");
                }
            }
        }
    }
    
    private Notification convertFromOld(tbax.shops.notification.Notification note, HashMap<tbax.shops.Shop, Integer> oldUids) {
        if (note instanceof tbax.shops.notification.BuyClaim) {
            BuyClaim newNote = new BuyClaim();
            tbax.shops.notification.BuyClaim oldNote = (tbax.shops.notification.BuyClaim)note;
            newNote.buyer = oldNote.buyer;
            newNote.entry = new BaxEntry(oldNote.entry);
            newNote.shop = state.getShop(oldUids.get(oldNote.shop));
            return newNote;
        }
        else if (note instanceof tbax.shops.notification.BuyNotification) {
            BuyNotification newNote = new BuyNotification();
            tbax.shops.notification.BuyNotification oldNote = (tbax.shops.notification.BuyNotification)note;
            newNote.buyer = oldNote.buyer;
            newNote.entry = new BaxEntry(oldNote.entry);
            newNote.shop = state.getShop(oldUids.get(oldNote.shop));
            return newNote;
        }
        else if (note instanceof tbax.shops.notification.BuyRejection) {
            BuyRejection newNote = new BuyRejection();
            tbax.shops.notification.BuyRejection oldNote = (tbax.shops.notification.BuyRejection)note;
            newNote.seller = oldNote.seller;
            newNote.entry = new BaxEntry(oldNote.entry);
            newNote.shop = state.getShop(oldUids.get(oldNote.shop));
            return newNote;
        }
        else if (note instanceof tbax.shops.notification.BuyRequest) {
            BuyRequest newNote = new BuyRequest();
            tbax.shops.notification.BuyRequest oldNote = (tbax.shops.notification.BuyRequest)note;
            newNote.buyer = oldNote.buyer;
            newNote.purchased = new BaxEntry(oldNote.purchased);
            newNote.expirationDate = oldNote.expirationDate;
            newNote.shop = state.getShop(oldUids.get(oldNote.shop));
            return newNote;
        }
        else if (note instanceof tbax.shops.notification.DeathNotification) {
            DeathNotification newNote = new DeathNotification();
            tbax.shops.notification.DeathNotification oldNote = (tbax.shops.notification.DeathNotification)note;
            newNote.person = "someone";
            newNote.tax = oldNote.tax;
            return newNote;
        }
        else if (note instanceof tbax.shops.notification.GeneralNotification) {
            GeneralNotification newNote = new GeneralNotification();
            tbax.shops.notification.GeneralNotification oldNote = (tbax.shops.notification.GeneralNotification)note;
            newNote.message = oldNote.message;
            return newNote;
        }
        else if (note instanceof tbax.shops.notification.LollipopNotification) {
            LollipopNotification newNote = new LollipopNotification();
            tbax.shops.notification.LollipopNotification oldNote = (tbax.shops.notification.LollipopNotification)note;
            newNote.sender = oldNote.sender;
            newNote.tastiness = oldNote.tastiness;
            return newNote;
        }
        else if (note instanceof tbax.shops.notification.SaleNotification) {
            SaleNotification newNote = new SaleNotification();
            tbax.shops.notification.SaleNotification oldNote = (tbax.shops.notification.SaleNotification)note;
            newNote.seller = oldNote.seller;
            newNote.entry = new BaxEntry(oldNote.entry);
            newNote.shop = state.getShop(oldUids.get(oldNote.shop));
            return newNote;
        }
        else if (note instanceof tbax.shops.notification.SaleNotificationAuto) {
            SaleNotificationAuto newNote = new SaleNotificationAuto();
            tbax.shops.notification.SaleNotificationAuto oldNote = (tbax.shops.notification.SaleNotificationAuto)note;
            newNote.seller = oldNote.seller;
            newNote.entry = new BaxEntry(oldNote.entry);
            newNote.shop = state.getShop(oldUids.get(oldNote.shop));
            return newNote;
        }
        else if (note instanceof tbax.shops.notification.SaleRejection) {
            SaleRejection newNote = new SaleRejection();
            tbax.shops.notification.SaleRejection oldNote = (tbax.shops.notification.SaleRejection)note;
            newNote.seller = oldNote.seller;
            newNote.entry = new BaxEntry(oldNote.entry);
            newNote.shop = state.getShop(oldUids.get(oldNote.shop));
            return newNote;
        }
        else if (note instanceof tbax.shops.notification.SellRequest) {
            SellRequest newNote = new SellRequest();
            tbax.shops.notification.SellRequest oldNote = (tbax.shops.notification.SellRequest)note;
            newNote.seller = oldNote.seller;
            newNote.entry = new BaxEntry(oldNote.entry);
            newNote.expirationDate = oldNote.expirationDate;
            newNote.shop = state.getShop(oldUids.get(oldNote.shop));
            return newNote;
        }
        return null;
    }
}
