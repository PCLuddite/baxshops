/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization;

import org.bukkit.OfflinePlayer;
import tbax.baxshops.ShopPlugin;

import java.util.UUID;

public final class ShopUser
{
    private UUID uuid;
    private String legacyName;

    public ShopUser(UUID uuid)
    {
        this.uuid = uuid;
    }

    @Deprecated
    public ShopUser(String name)
    {
        if (StoredPlayer.DUMMY_NAME.equalsIgnoreCase(name)) {
            uuid = StoredPlayer.DUMMY_UUID;
        }
        else {
            legacyName = name;
            ShopPlugin.getOfflinePlayer(name);
        }
    }

    public OfflinePlayer getOfflinePlayer()
    {
        if (uuid == null) {
            return ShopPlugin.getOfflinePlayer(legacyName).get(0);
        }
        else {
            return ShopPlugin.getOfflinePlayer(uuid);
        }
    }
}
