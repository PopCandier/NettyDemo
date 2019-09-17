package com.pop.netty.chapter_14.processor;

import com.pop.netty.chapter_14.entity.Header;
import com.pop.netty.chapter_14.utils.MessageType;
import com.pop.netty.chapter_14.entity.NettyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Pop
 * @date 2019/9/17 23:16
 * 心跳响应消息处理
 */
public class HeartBeatRespHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;

        //返回应答心跳应答消息
        if(message.getHeader()!=null&&
            message.getHeader().getType()== MessageType.PING_PONG_REQ.value()){
            //如果是心跳请求的
            System.out.println(" 收到了客户端的心跳请求 :"+message);
            ctx.writeAndFlush(buildHeatBeat());
        }
    }

    private NettyMessage buildHeatBeat(){
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.PING_PONG_RESP.value());
        message.setHeader(header);
        return message;
    }

}
