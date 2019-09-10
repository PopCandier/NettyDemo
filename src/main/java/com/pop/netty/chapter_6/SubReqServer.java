package com.pop.netty.chapter_6;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.awt.*;

/**
 * @author Pop
 * @date 2019/9/11 0:08
 */
public class SubReqServer {

    public void bind(int port){

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    /**
                                     * ObjectDecoder负责对实现了Serialize的POJO对象
                                     * 进行解码，他有多个构造函数，支持不同的ClassResolver
                                     * weakCachingConcurrentResolver，我们使用这个创建线程安全
                                     * WeakReferenceMap对类加载器进行缓存，当jvm内存不足的时候，
                                     * 将会释放缓存中的内存，放置内存泄露，为
                                     * 防止异常码流和解码错误位置内存溢出
                                     * 这里将单个对象最大序列化的字节数组长度设置为
                                     * 1M
                                     */
                                    new ObjectDecoder(1024*1024,
                                            ClassResolvers.weakCachingConcurrentResolver(
                                                    this.getClass().getClassLoader()
                                            ))
                            );
                            /**
                             * 会将消息的时候，自动将实现了Serializable的pojo对象
                             * 进行编码
                             */
                            ch.pipeline().addLast(new ObjectEncoder());
                            ch.pipeline().addLast(new SubReqServerHandler());
                        }
                    });
            ChannelFuture f = bootstrap.bind(port).sync();

            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new SubReqServer().bind(8080);
    }

}
