package com.pop.netty.chapter_11;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author Pop
 * @date 2019/9/14 19:16
 */
public class WebSocketServer {


    public void run(int port){

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup,workGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            /*
                            添加HttpServerCodec 将请求和应答消息或者编码为
                            Http消息
                             */
                            pipeline.addLast("http-codec",
                                    new HttpServerCodec());

                            pipeline.addLast("aggregator",
                                    new HttpObjectAggregator(65536));
                            /**
                             * 用来发送大容量的文件
                             */
                            pipeline.addLast("http-chunked",
                                    new ChunkedWriteHandler());

                            pipeline.addLast("handler",new WebSocketServerHandler());
                        }
                    });

            ChannelFuture future = b.bind(port).sync();

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }


    }

    public static void main(String[] args) {
        new WebSocketServer().run(8080);
    }

}
