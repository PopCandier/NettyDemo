package com.pop.netty.chapter_12;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;


/**
 * @author Pop
 * @date 2019/9/15 22:33
 */
public class ChineseProverbClient {

    public void run(int port){

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();

            b.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST,true)
                    .handler(new ChineseProverbClientHandler());

            Channel channel = b.bind(0).sync().channel();

            //向网段内所有的机器广播UDP消息
            channel.writeAndFlush(
                    new DatagramPacket(
                            Unpooled.copiedBuffer("随机数字", Charset.forName("utf-8")),
                            new InetSocketAddress("255.255.255.255",port))).sync();

            //15 s 回应
            if(!channel.closeFuture().await(15000)){
                System.out.println("查询超时");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new ChineseProverbClient().run(8080);
    }

}
