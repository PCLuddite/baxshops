package tbax.shops.serialization;

import java.io.*;
import tbax.shops.*;
import tbax.shops.notification.*;
import org.bukkit.*;
import java.util.*;

public class State2 implements Serializable
{
    private static final long serialVersionUID = 1L;
    public HashMap<BlockLocation, BaxShop> shops;
    public HashMap<String, ArrayDeque<Notification>> pending;

    public State2() {
        this.shops = new HashMap<>();
    }

    public HashMap<Location, BaxShop> getShops() {
        final HashMap<Location, BaxShop> deserialized = new HashMap<Location, BaxShop>();
        for (final Map.Entry<BlockLocation, BaxShop> entry : this.shops.entrySet()) {
            final BaxShop shop = entry.getValue();
            deserialized.put(shop.location = entry.getKey().toLocation(), shop);
        }
        return deserialized;
    }
}
