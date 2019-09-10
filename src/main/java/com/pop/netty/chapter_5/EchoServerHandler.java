package com.pop.netty.chapter_5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * @author Pop
 * @date 2019/9/10 22:18
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private int counter = 0;



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        String body = (String) msg;
        System.out.println("this is "+ ++counter+ "times receive client :["
        +body+"]");

        body+="$_";//回执给客户端的内容
        ByteBuf echo = Unpooled.copiedBuffer(body.getBytes());
        //分隔符
        ctx.writeAndFlush(echo);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
