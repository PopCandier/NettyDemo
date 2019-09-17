package com.pop.netty.chapter_14.processor;

import com.pop.netty.chapter_14.entity.Header;
import com.pop.netty.chapter_14.utils.MessageType;
import com.pop.netty.chapter_14.entity.NettyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Pop
 * @date 2019/9/17 22:48
 *
 * 心跳请求消息处理
 */
public class HeartBeatReqHandler extends ChannelInboundHandlerAdapter {

    private volatile ScheduledFuture<?> heartBeat;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;

        if(message.getHeader()!=null&&
            message.getHeader().getType()== MessageType.LOGIN_RESP.value()){
            //收到回执，开始发送心跳
            heartBeat=ctx.executor().scheduleAtFixedRate(new HeartBeatTask(ctx),
                    0,5000, TimeUnit.MILLISECONDS);
        }else if(message.getHeader()!=null&&
            message.getHeader().getType()==MessageType.PING_PONG_RESP.value()){
            //对于心跳回执的处理
            System.out.println(" 客户端已经收到了服务端发送的心跳信息 ："+message);
        }else{
            ctx.fireChannelRead(msg);
        }

    }

    private class HeartBeatTask implements Runnable{

        private final ChannelHandlerContext context;

        public HeartBeatTask(ChannelHandlerContext context) {
            this.context =context;
        }

        @Override
        public void run() {
            //在这类构造心跳请求
            NettyMessage heartBeat = buildHeatBeat();
            System.out.println(" 客户端发送心跳消息给服务端："+heartBeat);
            context.writeAndFlush(heartBeat);
        }
    }

    private NettyMessage buildHeatBeat(){

        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.PING_PONG_REQ.value());
        message.setHeader(header);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        //如果你的握手回执不拿到的话，视图是不可能初始化的
        if(heartBeat!=null){
            heartBeat.cancel(true);
            heartBeat = null;
        }
        ctx.fireExceptionCaught(cause);
    }
}
