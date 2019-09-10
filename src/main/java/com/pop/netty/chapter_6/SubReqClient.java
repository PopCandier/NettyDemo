package com.pop.netty.chapter_6;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @author Pop
 * @date 2019/9/11 0:27
 */
public class SubReqClient {


    public void connect(int port,String host){

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            /**
                             * 禁止对类加载器进行缓存
                             * 他在基于OSGI动态模块化编程中经常使用，
                             * 由于OSGI的bundle可以进行热部署和热升级
                             * 所以当某个bundle升级后，他对应的类加载器也会升级
                             * 所以，很少对类加载器进行缓存，因为他可能会发生变化
                             */
                            ch.pipeline().addLast(
                                    new ObjectDecoder(1024,
                                            ClassResolvers.cacheDisabled(
                                                    this.getClass().getClassLoader()
                                            ))
                            );

                            ch.pipeline().addLast(new ObjectEncoder());
                            ch.pipeline().addLast(new SubReqClientHandler());

                        }
                    });

            ChannelFuture future = bootstrap.connect(host,port).sync();

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new SubReqClient().connect(8080,"localhost");
    }
}
