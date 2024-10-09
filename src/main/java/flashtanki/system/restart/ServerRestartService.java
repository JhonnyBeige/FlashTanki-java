package flashtanki.system.restart;

import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.timer.runtime.TankRespawnScheduler;
import flashtanki.commands.Type;
import flashtanki.lobby.battles.BattleInfo;
import flashtanki.lobby.battles.BattlesList;
import flashtanki.logger.remote.types.LogType;
import flashtanki.logger.LoggerService;
import flashtanki.main.netty.NettyService;
import flashtanki.services.LobbysServices;
import flashtanki.services.annotations.ServicesInject;
import flashtanki.system.quartz.QuartzService;
import flashtanki.system.quartz.TimeType;
import flashtanki.system.quartz.impl.QuartzServiceImpl;
import flashtanki.users.locations.UserLocation;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

public class ServerRestartService {
    private static final ServerRestartService instance = new ServerRestartService();
    @ServicesInject(target=LobbysServices.class)
    private static final LobbysServices lobbysServices = LobbysServices.getInstance();
    @ServicesInject(target=QuartzServiceImpl.class)
    private static final QuartzService quartzService = QuartzServiceImpl.inject();
    @ServicesInject(target=NettyService.class)
    private static final NettyService nettyServices = NettyService.inject();
    private static final LoggerService loggerService = LoggerService.getInstance();

    public void restart() {
        lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.ALL, "server_halt");
        quartzService.addJob("ServerRestartJob", "Systems Jobs", e -> {
            TankRespawnScheduler.dispose();
            for (BattleInfo battle : BattlesList.getList()) {
                BattlefieldModel model = battle.model;
                if (model == null) continue;
                model.tanksKillModel.restartBattle(false);
            }
            quartzService.addJob("ServerRestartJob: Destroy", "Systems Jobs", e_ -> {
                nettyServices.destroy();
                loggerService.log(LogType.INFO,"Server can be shutdowning!");
                this.disableSystemOutput();
            }, TimeType.SEC, 10L);
        }, TimeType.SEC, 40L);
    }

    private void disableSystemOutput() {
        System.setOut(new PrintStream( new OutputStream(){

            @Override
            public void write(int b) {
            }
        }){

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }

            @Override
            public void write(int b) {
            }

            @Override
            public void write(byte[] b) {
            }

            @Override
            public void write(byte[] buf, int off, int len) {
            }

            @Override
            public void print(boolean b) {
            }

            @Override
            public void print(char c) {
            }

            @Override
            public void print(int i) {
            }

            @Override
            public void print(long l) {
            }

            @Override
            public void print(float f) {
            }

            @Override
            public void print(double d) {
            }

            @Override
            public void print(char[] s) {
            }

            @Override
            public void print(String s) {
            }

            @Override
            public void print(Object obj) {
            }

            @Override
            public void println() {
            }

            @Override
            public void println(boolean x) {
            }

            @Override
            public void println(char x) {
            }

            @Override
            public void println(int x) {
            }

            @Override
            public void println(long x) {
            }

            @Override
            public void println(float x) {
            }

            @Override
            public void println(double x) {
            }

            @Override
            public void println(char[] x) {
            }

            @Override
            public void println(String x) {
            }

            @Override
            public void println(Object x) {
            }

            @Override
            public PrintStream printf(String format, Object ... args) {
                return this;
            }

            @Override
            public PrintStream printf(Locale l, String format, Object ... args) {
                return this;
            }

            @Override
            public PrintStream format(String format, Object ... args) {
                return this;
            }

            @Override
            public PrintStream format(Locale l, String format, Object ... args) {
                return this;
            }

            @Override
            public PrintStream append(CharSequence csq) {
                return this;
            }

            @Override
            public PrintStream append(CharSequence csq, int start, int end) {
                return this;
            }

            @Override
            public PrintStream append(char c) {
                return this;
            }
        });
    }

    public static ServerRestartService inject() {
        return instance;
    }
}

