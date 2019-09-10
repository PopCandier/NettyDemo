package com.pop.netty.chapter_5;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author Pop
 * @date 2019/9/10 22:11
 */
public class EchoServer {

    public void bind(int port){

        //配置线程池
        EventLoopGroup bossgroup = new NioEventLoopGroup();
        EventLoopGroup workgroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossgroup,workgroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
                            /**
                             * 1024 表示单条消息的最大长度，当到达该长度后，仍然没有找到
                             * 分割符，抛出TooLongFrame异常
                             * 第二参数，放置异常码确实分割符导致内存溢出，这是分割福
                             * 的缓冲对象。
                             */
//                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(10,delimiter));
                            ch.pipeline().addLast(new FixedLengthFrameDecoder(4));
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new EchoServerHandler());

                        }
                    });

            ChannelFuture f = bootstrap.bind(port).sync();

            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossgroup.shutdownGracefully();
            workgroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new EchoServer().bind(8080);
    }

}
