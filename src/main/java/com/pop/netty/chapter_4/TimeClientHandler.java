package com.pop.netty.chapter_4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 * @author Pop
 * @date 2019/9/9 23:52
 */
public class TimeClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(TimeClientHandler.class.getName());

    private final ByteBuf firstMessage;

    private Charset charset = Charset.forName("utf-8");
    private byte[] req;
    public TimeClientHandler() {
        req = ("QUERY TIME ORDER"+System.getProperty("line.separator")).getBytes();

        firstMessage = Unpooled.buffer(req.length);
        firstMessage.writeBytes(req);

    }

private int counter;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(firstMessage);

        ByteBuf message = null;

        for (int i = 0; i <100 ; i++) {
            message = Unpooled.buffer(req.length);
            message.writeBytes(req);
            ctx.writeAndFlush(message);
        }

        /**
         * 当和服务器的TCP链路建立连接的时候，NettyNio线程会调用这个方法
         */
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        /**
         * 返回应答消息的时候
         */
//        ByteBuf buf = (ByteBuf) msg;
//        byte[] req = new byte[buf.readableBytes()];
//        buf.readBytes(req);
//        String body = new String(req,"utf-8");
        String body = (String) msg;


//        System.out.println("客户端收到信息 "+body);

        System.out.println("Now is :"+body+" ; the counter is "+ ++counter);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        logger.warning("发生了一个异常 "+cause.getMessage());
        ctx.close();
    }
}
