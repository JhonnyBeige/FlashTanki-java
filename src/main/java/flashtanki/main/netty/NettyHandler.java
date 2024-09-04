/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.main.netty;

import flashtanki.logger.LogType;
import flashtanki.logger.LoggerService;
import flashtanki.logger.RemoteDatabaseLogger;
import flashtanki.main.netty.blackip.model.BlackIPService;
import org.jboss.netty.channel.*;

import java.io.PrintWriter;
import java.io.StringWriter;

public class NettyHandler extends SimpleChannelUpstreamHandler {
    private final NettyUsersHandlerController controller = NettyUsersHandlerController.getInstance();
    private final static LoggerService loggerService = LoggerService.getInstance();
    private final BlackIPService blackList = BlackIPService.getInstance();

    private static NettyHandler instance;

    private NettyHandler() {
    }

    public static NettyHandler getInstance() {
        if (instance == null) {
            instance = new NettyHandler();
        }
        return instance;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        if (blackList.contains(ctx.getChannel().getRemoteAddress().toString().split(":")[0])) {
            ctx.getChannel().close();
            return;
        }
        this.controller.onClientConnected(ctx);
        this.log("Client connected from " + ctx.getChannel().getRemoteAddress() + " (" + ctx.getChannel().getId() + ")");
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
        this.log("Connection closed from " + ctx.getChannel().getRemoteAddress() + " (" + ctx.getChannel().getId() + ")");
        this.controller.onClientDisconnect(ctx);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) {
        this.controller.onMessageRecived(ctx, event);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        try {
            e.getCause().printStackTrace();
            StringWriter sw = new StringWriter();
            e.getCause().printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            RemoteDatabaseLogger.error(exceptionAsString);
            if (ctx.getChannel().isConnected()) {
                ctx.getChannel().close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            RemoteDatabaseLogger.error(ex);
        }
    }

    private void log(String txt) {
        loggerService.log(LogType.INFO, "[Netty]: " + txt);
    }
}

