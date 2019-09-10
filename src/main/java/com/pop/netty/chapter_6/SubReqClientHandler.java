package com.pop.netty.chapter_6;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Pop
 * @date 2019/9/11 0:31
 */
public class SubReqClientHandler extends ChannelInboundHandlerAdapter {

    public SubReqClientHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (int i = 0; i <10 ; i++) {
            ctx.write(subReq(i));
        }
        ctx.flush();
    }

    private SubscribeReq subReq(int i){

        SubscribeReq req = new SubscribeReq();
        req.setAddress("中国");
        req.setPhoneNumber("1233");
        req.setProductName("Netty");
        req.setSubReqID(i);
        req.setUserName("pop");
        return  req;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("接受到数据 "+msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();//将缓冲区的数据写到channelSocket中去
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
