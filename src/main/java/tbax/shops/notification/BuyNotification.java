package tbax.shops.notification;

import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.serialization.StateLoader;
import org.tbax.baxshops.serialization.StoredPlayer;
import org.tbax.baxshops.serialization.states.State_00050;
import tbax.shops.Shop;
import tbax.shops.ShopEntry;

public class BuyNotification implements Notification
{
    private static final long serialVersionUID = 1L;
    public ShopEntry entry;
    public Shop shop;
    public String buyer;

    public BuyNotification(final Shop shop, final ShopEntry entry, final String buyer) {
        this.shop = shop;
        this.entry = entry;
        this.buyer = buyer;
    }

    @Override
    public @NotNull Class<? extends org.tbax.baxshops.notification.Notification> getNewNoteClass()
    {
        return org.tbax.baxshops.notification.BuyNotification.class;
    }

    @Override
    public org.tbax.baxshops.notification.@NotNull Notification getNewNote(StateLoader stateLoader)
    {
        return new org.tbax.baxshops.notification.BuyNotification(
                ((State_00050)stateLoader).registerShop(shop),
                ((State_00050)stateLoader).registerPlayer(buyer),
                StoredPlayer.DUMMY,
                entry.modernize((State_00050)stateLoader)
        );
    }
}
