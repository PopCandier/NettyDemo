package com.pop.netty.chapter_5;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;


/**
 * @author Pop
 * @date 2019/9/10 23:08
 */
public class EchoClient {

    public void connect(int port,String host){

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

//                            ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
//                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(10,delimiter));
                            ch.pipeline().addLast(new FixedLengthFrameDecoder(4));
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new EchoClientHandler());

                        }
                    });
            //发起异步连接请求
            ChannelFuture f = b.connect(host,port).sync();
            //等待客户端链路关闭
            f.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new EchoClient().connect(8080,"localhost");
    }
}
