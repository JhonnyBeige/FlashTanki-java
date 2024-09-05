/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.main.netty;


import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import flashtanki.logger.LoggerService;
import flashtanki.system.quartz.TimeType;
import flashtanki.system.quartz.impl.QuartzServiceImpl;
import lombok.SneakyThrows;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class NettyUsersHandlerController {
    private Map<Channel, ProtocolTransfer> sessions = new HashMap<>();
    private Map<Long, ProtocolTransfer> authorizedSessions = new HashMap<>();
    private static QuartzServiceImpl quartzService = QuartzServiceImpl.getInstance();
    private static final LoggerService loggerService = LoggerService.getInstance();
    private static NettyUsersHandlerController instance;

    private NettyUsersHandlerController() {
        quartzService.addJobInterval("sessionDdosCleaner", "sessionDdosCleaner",
                jobExecutionContext -> sessions.values()
                .stream()
                .forEach(ProtocolTransfer::resetCountPackage), TimeType.SEC, 1);
    }

    public static NettyUsersHandlerController getInstance() {
        if (instance == null) {
            instance = new NettyUsersHandlerController();
        }
        return instance;
    }

    public void onClientConnected(ChannelHandlerContext ctx) {
        this.sessions.put(ctx.getChannel(), new ProtocolTransfer(ctx.getChannel()));
    }

    public void onClientDisconnect(ChannelHandlerContext ctx) {
        List<Long> authorizedSessionsForRemove = this.authorizedSessions.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getChannel().equals(ctx.getChannel()))
                .map(Map.Entry::getKey)
                .toList();

        assert authorizedSessionsForRemove.size() <= 1;
        authorizedSessionsForRemove.forEach(this.authorizedSessions::remove);


        this.sessions.remove(ctx.getChannel())
                .onDisconnect();
    }


    public void onMessageRecived(ChannelHandlerContext ctx, MessageEvent msg) {
        String message = (String) msg.getMessage();

        Channel channel = ctx.getChannel();
        this.sessions.get(channel)
                .decryptProtocol(decode(message));
    }

    private String decode(String message) {
        return decrypt(message, "084B255737229811CF454AF2AE99B20E", Base64.getEncoder().encodeToString("D8BF3DF78364B5CC".getBytes()));
    }

    @SneakyThrows
    public static String decrypt(String input, String key, String initVector)  {
        IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(initVector));
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        byte[] original = cipher.doFinal(Base64.getDecoder().decode(input));

        return new String(original);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public void authorizeSession(ProtocolTransfer protocolTransfer, long userId) {
        if (this.authorizedSessions.containsKey(userId)) {
            ProtocolTransfer activeProtocolTransfer = this.authorizedSessions.remove(userId);
            this.sessions.get(activeProtocolTransfer.getChannel()).onDisconnect();
            activeProtocolTransfer.closeConnection();
        }
        this.authorizedSessions.put(userId, protocolTransfer);
    }

    public String ddosLog(){
        return this.sessions.values()
                .stream()
                //DESC by total packate count
                .sorted((s1, s2) -> Long.compare(s2.getTotalPackageCount(), s1.getTotalPackageCount()))
                .map(protocolTransfer -> protocolTransfer.getTotalPackageCount() + " " + protocolTransfer.getIP()+ " " + protocolTransfer.getParam("userId"))
                .collect(Collectors.joining("|"));
    }
}

