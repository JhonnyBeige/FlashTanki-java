/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.managers;

import flashtanki.battles.maps.Map;
import flashtanki.battles.tanks.math.Vector3;
import java.util.Random;

public class SpawnManager {
    private static Random rand = new Random();

    public static Vector3 getSpawnState(Map map, String forTeam) {
        Vector3 pos = null;
        try {
            pos = forTeam.equals("BLUE") ? map.spawnPositonsBlue.get(rand.nextInt(map.spawnPositonsBlue.size())) : (forTeam.equals("RED") ? map.spawnPositonsRed.get(rand.nextInt(map.spawnPositonsRed.size())) : map.spawnPositonsDM.get(rand.nextInt(map.spawnPositonsDM.size())));
            if (pos == null) {
                pos = map.spawnPositonsDM.get(rand.nextInt(map.spawnPositonsDM.size()));
            }
        }
        catch (Exception ex) {
            pos = map.spawnPositonsDM.get(rand.nextInt(map.spawnPositonsDM.size()));
        }
        return pos;
    }
}

