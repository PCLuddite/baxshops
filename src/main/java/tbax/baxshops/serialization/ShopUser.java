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
    }

    public boolean isLegacy()
    {
        return uuid == null;
    }

    public OfflinePlayer getOfflinePlayer()
    {
        if (uuid == null) {
            return StoredData.getOfflinePlayer(legacyName);
        }
        else {
            return StoredData.getOfflinePlayer(uuid);
        }
    }
}
