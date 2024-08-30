/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.timer.schedulers.runtime;

import flashtanki.utils.StringUtils;
import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.managers.SpawnManager;
import flashtanki.battles.tanks.Tank;
import flashtanki.battles.tanks.TankState;
import flashtanki.battles.tanks.math.Vector3;
import flashtanki.commands.Type;
import flashtanki.json.JSONUtils;
import flashtanki.logger.RemoteDatabaseLogger;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class TankRespawnScheduler {
    private static final Timer TIMER = new Timer("TankRespawnScheduler timer");
    private static final long TIME_TO_PREPARE_SPAWN = 3000L;
    private static final long TIME_TO_SPAWN = 5000L;
    private static HashMap<BattlefieldPlayerController, PrepareToSpawnTask> tasks = new HashMap();
    private static boolean disposed;

    public static void startRespawn(BattlefieldPlayerController player, boolean onlySpawn) {
        if (disposed) {
            return;
        }
        try {
            if (player == null) {
                return;
            }
            if (player.battle == null) {
                return;
            }
            PrepareToSpawnTask task = new PrepareToSpawnTask();
            task.player = player;
            task.onlySpawn = onlySpawn;
            tasks.put(player, task);
            TIMER.schedule((TimerTask)task, onlySpawn ? 1L : TIME_TO_PREPARE_SPAWN);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            RemoteDatabaseLogger.error(ex);
        }
    }

    public static void dispose() {
        disposed = true;
    }

    public static void cancelRespawn(BattlefieldPlayerController player) {
        try {
            PrepareToSpawnTask task = tasks.get(player);
            if (task == null) {
                return;
            }
            if (task.spawnTask == null) {
                task.cancel();
            } else {
                task.spawnTask.cancel();
            }
            tasks.remove(player);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            RemoteDatabaseLogger.error(ex);
        }
    }

    static class PrepareToSpawnTask
    extends TimerTask {
        public SpawnTask spawnTask;
        public BattlefieldPlayerController player;
        public Vector3 preparedPosition;
        public boolean onlySpawn;

        PrepareToSpawnTask() {
        }

        @Override
        public void run() {
            try {
                if (this.player == null) {
                    return;
                }
                if (this.player.tank == null) {
                    return;
                }
                if (this.player.battle == null) {
                    return;
                }
                this.preparedPosition = SpawnManager.getSpawnState(this.player.battle.battleInfo.map, this.player.playerTeamType);
                if (this.onlySpawn) {
                    this.player.send(Type.BATTLE, "prepare_to_spawn", StringUtils.concatStrings(this.player.tank.id, ";", String.valueOf(this.preparedPosition.x), "@", String.valueOf(this.preparedPosition.y), "@", String.valueOf(this.preparedPosition.z), "@", String.valueOf(this.preparedPosition.rot)));
                } else {
                    if (this.player.battle == null) {
                        return;
                    }
                    this.player.tank.position = this.preparedPosition;
                    this.player.send(Type.BATTLE, "prepare_to_spawn", StringUtils.concatStrings(this.player.tank.id, ";", String.valueOf(this.preparedPosition.x), "@", String.valueOf(this.preparedPosition.y), "@", String.valueOf(this.preparedPosition.z), "@", String.valueOf(this.preparedPosition.rot)));
                }
                this.spawnTask = new SpawnTask();
                this.spawnTask.preparedSpawnTask = this;
                TIMER.schedule((TimerTask)this.spawnTask, TIME_TO_SPAWN);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                RemoteDatabaseLogger.error(ex);
            }
        }
    }

    static class SpawnTask
    extends TimerTask {
        PrepareToSpawnTask preparedSpawnTask;

        SpawnTask() {
        }

        @Override
        public void run() {
            try {
                BattlefieldPlayerController player = this.preparedSpawnTask.player;
                if (player == null) {
                    return;
                }
                if (player.tank == null) {
                    return;
                }
                if (player.battle == null) {
                    return;
                }
                player.battle.tanksKillModel.changeHealth(player.tank, Tank.MAX_HEALTH_TANK);
                player.battle.sendToAllPlayers(Type.BATTLE, "spawn", JSONUtils.parseSpawnCommand(player, this.preparedSpawnTask.preparedPosition));
                player.tank.state = TankState.newcome;
                tasks.remove(player);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                RemoteDatabaseLogger.error(ex);
            }
        }
    }
}

