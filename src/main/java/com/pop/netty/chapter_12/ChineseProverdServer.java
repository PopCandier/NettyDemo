package com.pop.netty.chapter_12;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;


/**
 * @author Pop
 * @date 2019/9/15 22:16
 */
public class ChineseProverdServer {

    public void run(int port){

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            //NioDatagramChannel	异步非阻塞的 UDP Socket 连接
            b.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST,true)
                    .handler(new ChineseProverbServerHandler());
            b.bind(port).sync().channel().closeFuture().await();
            /**
             * 相比于Tcp通信，UDP不存在客户端和服务端的实际连接
             * 因此不需要为连接创建ChannelPipeline设置handler
             * 对于服务端而言，只需要设置启动服务类的handler即可
             */
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new ChineseProverdServer().run(8080);
    }
}
