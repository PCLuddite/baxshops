/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization;

import org.bukkit.OfflinePlayer;

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
        legacyName = name;
        StoredData.getOfflinePlayer(name);
    }

    public OfflinePlayer getOfflinePlayer()
    {
        if (uuid == null) {
            return StoredData.getOfflinePlayer(legacyName).get(0);
        }
        else {
            return StoredData.getOfflinePlayer(uuid);
        }
    }
}
