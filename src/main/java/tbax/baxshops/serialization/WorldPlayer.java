/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization;

import java.util.UUID;

public final class WorldPlayer extends StoredPlayer
{
    private static final String WORLD_UUID_STRING = "326a36ea-b465-3192-a4f7-c313f347edc9";
    private static final String WORLD_NAME_STRING = "world";

    public static final WorldPlayer PLAYER = new WorldPlayer();

    private WorldPlayer()
    {
        super(UUID.fromString(WORLD_UUID_STRING));
    }

    @Override
    public String getName()
    {
        return WORLD_NAME_STRING;
    }
}
