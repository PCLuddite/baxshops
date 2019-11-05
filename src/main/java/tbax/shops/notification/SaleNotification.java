package tbax.shops.notification;

import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.serialization.StateLoader;
import org.tbax.baxshops.serialization.states.State_00050;
import tbax.shops.BaxShop;
import tbax.shops.Shop;
import tbax.shops.ShopEntry;

public class SaleNotification implements Notification
{
    private static final long serialVersionUID = 1L;
    public ShopEntry entry;
    public Shop shop;
    public String seller;

    public SaleNotification(final Shop shop, final ShopEntry entry, final String seller) {
        this.shop = shop;
        this.entry = entry;
        this.seller = seller;
    }

    @Override
    public @NotNull Class<? extends org.tbax.baxshops.notification.Notification> getNewNoteClass()
    {
        return org.tbax.baxshops.notification.BuyNotification.class;
    }

    @Override
    public @NotNull org.tbax.baxshops.notification.Notification getNewNote(StateLoader stateLoader)
    {
        return new org.tbax.baxshops.notification.BuyNotification(
                ((State_00050)stateLoader).registerShop(shop),
                ((State_00050)stateLoader).registerPlayer(shop.owner),
                ((State_00050)stateLoader).registerPlayer(seller),
                entry.modernize((State_00050)stateLoader)
        );
    }
}
