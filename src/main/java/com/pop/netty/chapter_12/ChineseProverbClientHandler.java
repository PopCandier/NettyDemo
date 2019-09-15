package com.pop.netty.chapter_12;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.nio.charset.Charset;


/**
 * @author Pop
 * @date 2019/9/15 22:44
 */
public class ChineseProverbClientHandler extends
        SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
       cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        String response = msg.content().toString(Charset.forName("utf-8"));
        if(response.startsWith("随机数字: ")){
            System.out.println(response);
            ctx.close();
        }
        /**
         * 这里很简单，接受到服务端的消息后，将其转换为字符串，然后判断是否是
         * 随机数字: 开头，因为我们服务端是这样的
         * 如果没有发生丢包，所以数据是完整的
         */
    }
}
