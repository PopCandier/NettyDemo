package com.pop.netty.chapter_14.processor;


import com.pop.netty.chapter_14.entity.Header;
import com.pop.netty.chapter_14.utils.MessageType;
import com.pop.netty.chapter_14.entity.NettyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Pop
 * @date 2019/9/17 21:59
 * 握手和安全认证
 * 握手的发起是在客户端和服务端TCP链路成功通道激活时，握手消息的接入和安全认证在服务端处理

    request handler 处理的是响应的请求
 */
public class LoginAuthReqHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage message = (NettyMessage) msg;
        //如果是握手应答请求，需要判断是否认证成功
        if(message.getHeader()!=null&&
            message.getHeader().getType()== MessageType.LOGIN_RESP.value()){

            byte loginResult = (byte) message.getBody();
            if(loginResult<(byte)0){
                //握手失败，关闭连接
                ctx.close();
            }else{
                //这里简单的认为，如果body存非1的值，即使表示验证成功
                System.out.println("Login is ok "+message);
                ctx.fireChannelRead(msg);//意味着可以继续传播了
            }

        }else{
            ctx.fireChannelRead(msg);
        }

    }

    private NettyMessage buildLoginReq(){
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.LOGIN_REQ.value());
        message.setHeader(header);
        return message;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       //表示客户端连接成功后，我们需要进行权限认证 握手请求发送
        ctx.writeAndFlush(buildLoginReq());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
