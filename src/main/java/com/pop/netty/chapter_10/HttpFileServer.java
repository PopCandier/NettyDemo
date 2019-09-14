package com.pop.netty.chapter_10;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author Pop
 * @date 2019/9/14 15:32
 */
public class HttpFileServer {

    private static final String DEFUALT_URL = "/src/main/netty/file";

    public void run(final int port,final String url){

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();


        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //请求的解码
                            ch.pipeline().addLast("http-decoder",
                                    new HttpRequestDecoder());
                            // 2^16
                            /**
                             * 这个的作用就是将多个消息，转化为单一的
                             * FullHttpRequest 或者 FullHttpReponse
                             */
                            ch.pipeline().addLast("http-aggregator",
                                    new HttpObjectAggregator(65536));
                            //响应的编码
                            ch.pipeline().addLast("http-encoder",
                                    new HttpResponseEncoder());
                            /**
                             * 支持异步发送大的流码，例如大的文件传输
                             * 但不占用过多内存，防止发生java内存溢出
                             */
                            ch.pipeline().addLast("http-chunked",
                                    new ChunkedWriteHandler());

                            ch.pipeline().addLast(new HttpFileServerHandler(url));


                        }
                    });

            ChannelFuture future = b.bind(port).sync();

            System.out.println(" 文件服务器已经启动");

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new HttpFileServer().run(8080,DEFUALT_URL);
    }

}
