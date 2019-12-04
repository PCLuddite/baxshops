/*
 * Copyright © 2012 Nathan Dinsmore and Sam Lazarus
 * Modifications Copyright © Timothy Baxendale
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package tbax.shops.notification;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.serialization.internal.StateLoader;
import org.tbax.baxshops.serialization.StoredPlayer;
import org.tbax.baxshops.serialization.internal.states.State_00100;

import java.util.LinkedHashMap;
import java.util.Map;

public class LollipopNotification implements Notification
{
    private static final long serialVersionUID = 1L;
    public static final double DEFAULT_TASTINESS = 40.0;
    public static final Map<Double, String> adjectives;
    public String sender;
    public double tastiness;

    public static final String JSON_TYPE_ID = "lolly";

    public LollipopNotification(final String sender, final double tastiness) {
        this.sender = sender;
        this.tastiness = ((tastiness < 0.0) ? 0.0 : ((tastiness > 100.0) ? 100.0 : tastiness));
    }

    public LollipopNotification(JsonObject o) {
        sender = o.get("sender").getAsString();
        tastiness = o.get("tastiness").getAsInt();
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
        return org.tbax.baxshops.notification.internal.LollipopNotification.class;
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
        return new org.tbax.baxshops.notification.internal.LollipopNotification(
                ((State_00100)stateLoader).registerPlayer(sender),
                StoredPlayer.ERROR,
                adjective
        );
    }
}
