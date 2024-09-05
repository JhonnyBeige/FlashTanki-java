package flashtanki.battles.ctf.flags;

import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.ctf.FlagReturnTimer;
import flashtanki.battles.ctf.flags.FlagState;
import flashtanki.battles.tanks.math.Vector3;

public class FlagServer {
    public String flagTeamType;
    public BattlefieldPlayerController owner;
    public Vector3 position;
    public Vector3 basePosition;
    public FlagState state;
    public FlagReturnTimer returnTimer;
}
