/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.main.netty;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

public class NettyPipelineFactory implements ChannelPipelineFactory {
    @Override
    public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = Channels.pipeline();

        // Декодер, который будет разделять входящие сообщения на пакеты
        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));

        // Декодер, который будет преобразовывать байты в строки
        pipeline.addLast("stringDecoder", new StringDecoder());

        // Обработчик, который будет обрабатывать события и сообщения
        pipeline.addLast("handler", NettyHandler.getInstance());

        // Кодировщик, который будет преобразовывать строки в байты
        pipeline.addLast("lengthPrepender", new LengthFieldPrepender(4));
        pipeline.addLast("stringEncoder", new StringEncoder());

        // Кодировщик, который будет добавлять длину к каждому исходящему сообщению

        return pipeline;
    }
}

