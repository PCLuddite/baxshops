package tbax.shops.notification;

import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.serialization.StateLoader;
import org.tbax.baxshops.serialization.states.State_00050;
import tbax.shops.Shop;
import tbax.shops.ShopEntry;

import java.util.Calendar;
import java.util.Date;

public class SellRequest implements Request, TimedNotification
{
    private static final long serialVersionUID = 1L;
    public ShopEntry entry;
    public Shop shop;
    public long expirationDate;
    public String seller;

    public SellRequest(final Shop shop, final ShopEntry entry, final String seller) {
        this.shop = shop;
        this.entry = entry;
        this.seller = seller;
        final Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(5, 5);
        this.expirationDate = c.getTimeInMillis();
    }

    @Override
    public long expirationDate() {
        return this.expirationDate;
    }

    @Override
    public @NotNull Class<? extends org.tbax.baxshops.notification.Notification> getNewNoteClass()
    {
        return org.tbax.baxshops.notification.SaleRejection.class;
    }

    @Override
    public @NotNull org.tbax.baxshops.notification.Notification getNewNote(StateLoader stateLoader)
    {
        return new org.tbax.baxshops.notification.SaleRejection(
                ((State_00050)stateLoader).registerShop(shop),
                ((State_00050)stateLoader).registerPlayer(shop.owner),
                ((State_00050)stateLoader).registerPlayer(seller),
                entry.modernize((State_00050)stateLoader)
        );
    }
}
