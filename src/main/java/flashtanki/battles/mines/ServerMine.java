/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.mines;

import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.tanks.math.Vector3;

public class ServerMine {
    private String id;
    private Vector3 position;
    private BattlefieldPlayerController owner;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Vector3 getPosition() {
        return this.position;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
    }

    public BattlefieldPlayerController getOwner() {
        return this.owner;
    }

    public void setOwner(BattlefieldPlayerController owner) {
        this.owner = owner;
    }
}

