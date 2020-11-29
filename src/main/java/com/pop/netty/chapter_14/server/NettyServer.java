package com.pop.netty.chapter_14.server;

import com.pop.netty.chapter_14.processor.HeartBeatRespHandler;
import com.pop.netty.chapter_14.processor.LoginAuthRespHandler;
import com.pop.netty.chapter_14.protocol.NettyMessageDecoder;
import com.pop.netty.chapter_14.protocol.NettyMessageEncoder;
import com.pop.netty.chapter_14.utils.NettyConstant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * @program: nio-netty
 * @description:
 * @author: Pop
 * @create: 2019-09-18 10:50
 **/
public class NettyServer {

    public void bind() throws InterruptedException {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup,workerGroup).
                channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,100)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();

                        pipeline.addLast(new NettyMessageDecoder(1024*1024,
                                4,4,0,0));
                        pipeline.addLast(new NettyMessageEncoder());

                        pipeline.addLast("readTimeoutHandler",
                                new ReadTimeoutHandler(50));

                        pipeline.addLast(new LoginAuthRespHandler());



                        pipeline.addLast("heartBeatHandler",
                                new HeartBeatRespHandler());

                    }
                });

            b.bind(NettyConstant.REMOTEHOST,NettyConstant.REMOTEPORT).sync();
            System.out.println("Netty 服务器已经启动 "+NettyConstant.REMOTEHOST+" "+NettyConstant.REMOTEPORT);

    }

    public static void main(String[] args) throws InterruptedException {
        new NettyServer().bind();
    }

}
