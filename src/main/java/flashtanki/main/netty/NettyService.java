/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.main.netty;

import flashtanki.system.destroy.Destroyable;
import flashtanki.configurator.osgi.OSGi;
import flashtanki.configurator.server.configuration.entitys.NettyConfiguratorEntity;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

public class NettyService implements Destroyable {
    private static NettyService instance;
    private int port;
    private final ServerBootstrap bootstrap;
    public static NettyService getInstance() {
        if (instance == null) {
            instance = new NettyService();
        }
        return instance;
    }

    private NettyService() {
        this.initParams();
        OrderedMemoryAwareThreadPoolExecutor bossExec = new OrderedMemoryAwareThreadPoolExecutor(1, 400000000L, 2000000000L, 60L, TimeUnit.SECONDS);
        OrderedMemoryAwareThreadPoolExecutor ioExec = new OrderedMemoryAwareThreadPoolExecutor(4, 400000000L, 2000000000L, 60L, TimeUnit.SECONDS);
        NioServerSocketChannelFactory factory = new NioServerSocketChannelFactory((Executor) bossExec, ioExec, 4);
        this.bootstrap = new ServerBootstrap(factory);
        this.bootstrap.setPipelineFactory(new NettyPipelineFactory());
        this.bootstrap.setOption("child.tcpNoDelay", true);
        this.bootstrap.setOption("child.keepAlive", true);
    }

    public static NettyService inject() {
        return instance;
    }

    public void init() {
        this.bootstrap.bind(new InetSocketAddress(this.port));
    }

    @Override
    public void destroy() {
        System.exit(0);
        this.bootstrap.releaseExternalResources();
    }

    private void initParams() {
        this.port = ((NettyConfiguratorEntity) OSGi.getModelByInterface(NettyConfiguratorEntity.class)).getPort();
    }
}

