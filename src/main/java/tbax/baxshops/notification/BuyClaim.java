/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.Format;
import tbax.baxshops.Resources;
import tbax.baxshops.commands.ShopCmdActor;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.StoredData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class BuyClaim implements Claimable
{
    private BaxEntry entry;
    private UUID buyer;
    private UUID seller;
    private UUID shopId;

    public BuyClaim(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        this.shopId = shopId;
        this.buyer = buyer.getUniqueId();
        this.seller = seller.getUniqueId();
        this.entry = new BaxEntry(entry);
    }

    public BuyClaim(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        shopId = map.getUUID("shopId");
        buyer = map.getUUID("buyer");
        seller = map.getUUID("seller");
        entry = map.getBaxEntry("entry");
    }

    public BuyClaim(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        this(shopId, StoredData.getOfflinePlayer(buyer), StoredData.getOfflinePlayer(seller), entry);
    }

    public OfflinePlayer getSeller()
    {
        return StoredData.getOfflinePlayer(seller);
    }

    public UUID getShopId()
    {
        return shopId;
    }

    public OfflinePlayer getBuyer()
    {
        return StoredData.getOfflinePlayer(buyer);
    }

    @Override
    public BaxEntry getEntry()
    {
        return entry;
    }

    @Override
    public boolean claim(ShopCmdActor actor)
    {
        try {
            int overflow = actor.giveItem(entry.toItemStack(), false);
            if (overflow > 0) {
                actor.sendMessage(Resources.SOME_ROOM, entry.getAmount() - overflow, entry.getName());
                entry.setAmount(overflow);
                return false;
            }
            return true;
        }
        catch (PrematureAbortException e) {
            actor.sendMessage(e.getMessage());
            return false;
        }
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getBuyer().equals(sender)) {
            return String.format("%s accepted your request to buy %s for %s.",
                Format.username(seller),
                entry.getFormattedName(),
                entry.getFormattedBuyPrice()
            );
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s accepted %s's request to buy %s for %s.",
            Format.username(seller),
            Format.username2(buyer),
            entry.getFormattedName(),
            entry.getFormattedBuyPrice()
        );
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> args = new HashMap<>();
        args.put("shopId", shopId.toString());
        args.put("buyer", getBuyer().getUniqueId().toString());
        args.put("seller", getSeller().getUniqueId().toString());
        args.put("entry", entry);
        return args;
    }

    public static BuyClaim deserialize(Map<String, Object> args)
    {
        return new BuyClaim(args);
    }

    public static BuyClaim valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
