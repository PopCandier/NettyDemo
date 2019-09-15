package com.pop.netty.chapter_12;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;


import java.nio.charset.Charset;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Pop
 * @date 2019/9/15 22:22
 */
public class ChineseProverbServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final String [] DICTIONARY={
            "11111","22222","33333"
    };

    private String nextQuote(){
        int quoteId = ThreadLocalRandom.current().nextInt(DICTIONARY.length);
        return DICTIONARY[quoteId];
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        String req = packet.content().toString(Charset.forName("utf-8"));
        System.out.println(req);
        if("随机数字".equals(req)){
            //构造的内容是DatagramPack 第一个是要发送的内容，第二是发送的ip地址和端口
            ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(
                    "随机数字: "+nextQuote()
            ,Charset.forName("utf-8")),packet.sender()));//加上发送来到地址
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }
}
