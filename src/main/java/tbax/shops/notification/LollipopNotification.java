package tbax.shops.notification;

import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.serialization.StateLoader;
import org.tbax.baxshops.serialization.StoredPlayer;
import org.tbax.baxshops.serialization.states.State_00050;

import java.util.LinkedHashMap;
import java.util.Map;

public class LollipopNotification implements Notification
{
    private static final long serialVersionUID = 1L;
    public static final double DEFAULT_TASTINESS = 40.0;
    public static final Map<Double, String> adjectives;
    public String sender;
    public double tastiness;

    public LollipopNotification(final String sender, final double tastiness) {
        this.sender = sender;
        this.tastiness = ((tastiness < 0.0) ? 0.0 : ((tastiness > 100.0) ? 100.0 : tastiness));
    }

    static {
        (adjectives = new LinkedHashMap<Double, String>()).put(0.0, "a disgusting");
        LollipopNotification.adjectives.put(10.0, "a bad");
        LollipopNotification.adjectives.put(20.0, "an icky");
        LollipopNotification.adjectives.put(30.0, "a bland");
        LollipopNotification.adjectives.put(40.0, "a");
        LollipopNotification.adjectives.put(50.0, "an OK");
        LollipopNotification.adjectives.put(55.0, "a better-than-average");
        LollipopNotification.adjectives.put(60.0, "a good");
        LollipopNotification.adjectives.put(70.0, "a great");
        LollipopNotification.adjectives.put(80.0, "a tasty");
        LollipopNotification.adjectives.put(90.0, "a delicious");
        LollipopNotification.adjectives.put(99.0, "a wonderful");
    }

    @Override
    public @NotNull Class<? extends org.tbax.baxshops.notification.Notification> getNewNoteClass()
    {
        return org.tbax.baxshops.notification.LollipopNotification.class;
    }

    @Override
    public @NotNull org.tbax.baxshops.notification.Notification getNewNote(StateLoader stateLoader)
    {
        String adjective = "";
        for (Map.Entry<Double, String> entry : adjectives.entrySet()) {
            if (tastiness >= entry.getKey()) {
                adjective = entry.getValue();
            }
        }
        return new org.tbax.baxshops.notification.LollipopNotification(
                ((State_00050)stateLoader).registerPlayer(sender),
                StoredPlayer.ERROR,
                adjective
        );
    }
}
