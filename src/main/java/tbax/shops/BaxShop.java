package tbax.shops;

import java.io.Serializable;
import java.util.HashMap;

/**
 * A Shop represents a user's shop and its inventory of items.
 */
public class BaxShop extends Shop implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public BaxShop() {
    }
    
    public int getIndexOfEntry(int id, int damage) {
        for(int index = 0; index < inventory.size(); index++) {
            if (inventory.get(index).itemID == id && inventory.get(index).itemDamage == damage) {
                return index;
            }
        }
        return -1;
    }
    
    /**
     * The flag options set for this shop
     */
    public HashMap<String, Object> flags = new HashMap<>();

    public Object getOption(String flagName) {
        Object o = flags.get(flagName);
        if (o == null) {
            return false;
        }
        return o;
    }
    
    public Object setOption(String flagName, Object option) {
        return flags.put(flagName, option);
    }
}
