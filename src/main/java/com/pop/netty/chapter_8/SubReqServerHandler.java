package com.pop.netty.chapter_8;

import com.pop.netty.chapter_6.SubscribeReq;
import com.pop.netty.chapter_6.SubscribeResp;
import com.sun.xml.internal.ws.util.ByteArrayBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Pop
 * @date 2019/9/11 0:15
 */
public class SubReqServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到消息");
        SubscribeReq req = (SubscribeReq) msg;
        if("pop".equalsIgnoreCase(req.getUserName())){//权限认证
            System.out.println("Server 认证成功");
            ctx.writeAndFlush(resp(req.getSubReqID()));
        }
//        ByteBuf buf = (ByteBuf) msg;
//        byte[] bytes = new byte[buf.readableBytes()];
//        buf.readBytes(bytes);
//        System.out.println(new String(bytes,"utf-8"));
        ctx.writeAndFlush("我收到了消息");

    }

    private SubscribeResp resp(int subReqID){

        SubscribeResp resp = new SubscribeResp();
        resp.setSubReqID(subReqID);
        resp.setRespCode(200);
        resp.setDesc("一段成功的编码");
        return resp;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
       cause.printStackTrace();
       ctx.close();
    }
}
