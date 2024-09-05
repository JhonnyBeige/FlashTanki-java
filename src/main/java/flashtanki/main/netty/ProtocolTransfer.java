package flashtanki.main.netty;

import flashtanki.utils.StringUtils;
import flashtanki.authorization.AuthorizationService;
import flashtanki.commands.Command;
import flashtanki.commands.Commands;
import flashtanki.commands.Type;
import flashtanki.lobby.LobbyManager;
import flashtanki.logger.LogType;
import flashtanki.logger.LoggerService;
import flashtanki.system.SystemClientMessagesHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jboss.netty.channel.Channel;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ProtocolTransfer {
    public static final AuthorizationService AUTHORIZATION_SERVICE = AuthorizationService.getInstance();
    private static final LoggerService loggerService = LoggerService.getInstance();
    private static final NettyUsersHandlerController nettyUsersHandlerController = NettyUsersHandlerController
            .getInstance();
    private static final SystemClientMessagesHandler systemClientMessagesHandler = SystemClientMessagesHandler
            .getInstance();
    private static final String encryptionKey = "084B255737229811CF454AF2AE99B20E";
    private static final String encryptionIv = "D8BF3DF78364B5CC";
    private static final int MAX_PACKAGE_COUNT_PS = 10;
    public LobbyManager lobby;
    private Channel channel;
    private Map<String, Object> sessionData = new HashMap<>();
    private int countPackageLastSec = 0;
    @Getter
    private long totalPackageCount = 0;

    public ProtocolTransfer(Channel channel) {
        this.channel = channel;
    }

    public void resetCountPackage() {
        this.countPackageLastSec = 0;
    }

    public void decryptProtocol(String request) {
        if (!request.contains("move;")) {
            this.countPackageLastSec++;
            if (this.countPackageLastSec > MAX_PACKAGE_COUNT_PS) {
                loggerService.log(LogType.WARNING,
                        "Session DDOS detected " + this.countPackageLastSec + " " + this.getIP());
                this.closeConnection();
                return;
            }
        }
        Command cmd = Commands.decrypt(request);
        totalPackageCount++;

        switch (cmd.type) {
            case AUTH, PING, REGISTRATON: {
                AUTHORIZATION_SERVICE.executeCommand(cmd, this);
                break;
            }
            case CHAT, BATTLE, GARAGE, LOBBY_CHAT, LOBBY: {
                this.lobby.executeCommand(cmd);
                break;
            }
            case HTTP: {
                break;
            }
            case SYSTEM: {
                systemClientMessagesHandler.executeCommand(cmd, this);
                AUTHORIZATION_SERVICE.executeCommand(cmd, this);
                if (this.lobby == null)
                    break;
                this.lobby.executeCommand(cmd);
                break;
            }
            case UNKNOWN: {
                loggerService.log(LogType.INFO, "User " + this.channel.toString() +
                        " send unknowed request: " + cmd);
            }
        }
    }

    public boolean send(Type type, String... args) throws IOException {
        StringBuilder request = new StringBuilder();
        request.append(type.toString());
        request.append(";");
        for (int i = 0; i < args.length - 1; ++i) {
            request.append(StringUtils.concatStrings(args[i], ";"));
        }
        request.append(args[args.length - 1]);
        if (this.channel.isWritable() && this.channel.isConnected() && this.channel.isOpen()) {
            this.channel.write(encode(request.toString(), encryptionKey, Base64.getEncoder().encodeToString(encryptionIv.getBytes())));
        }
        request = null;
        return true;
    }

    @SneakyThrows
    private String encode(String message, String key, String initVector) {
        IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(initVector));
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

        byte[] original = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(original);
    }

    protected void onDisconnect() {
        if (this.lobby != null) {
            this.lobby.onDisconnect();
        }
    }

    public void closeConnection() {
        this.channel.close();
    }

    public String getIP() {
        SocketAddress remoteAddress = this.channel.getRemoteAddress();
        return remoteAddress.toString();
    }

    public <T> T getParam(String key) {
        try {
            return (T) sessionData.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setParam(String key, Object value) {
        sessionData.put(key, value);
    }

    public void identify(long id) {
        nettyUsersHandlerController.authorizeSession(this, id);
        this.setParam("userId", id);
    }

    protected Channel getChannel() {
        return this.channel;
    }
}
