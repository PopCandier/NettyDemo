package com.pop.netty.chapter_8;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


/**
 * @author Pop
 * @date 2019/9/12 0:09
 */
public class SubReqClient {

    public void connect(int port,String host){

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    MarshallingCodeCFactory.buildMarshllingDecoder()
                            )     ;
                            ch.pipeline().addLast(MarshallingCodeCFactory
                                    .buildMarshallingEncoder());

                            ch.pipeline().addLast(new SubReqClientHandler());
                        }
                    });

            ChannelFuture future = b.connect(host,port).sync();

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
