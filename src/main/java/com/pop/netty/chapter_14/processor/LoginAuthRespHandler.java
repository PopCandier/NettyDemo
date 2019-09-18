package com.pop.netty.chapter_14.processor;

import com.pop.netty.chapter_14.entity.Header;
import com.pop.netty.chapter_14.utils.MessageType;
import com.pop.netty.chapter_14.entity.NettyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Pop
 * @date 2019/9/17 22:30
 *
 * 服务端的握手接入和安全认证代码
 */
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {

    private Map<String,Boolean> nodeCheck =
            new ConcurrentHashMap<>();

    //白名单
    private String[] whiteList={"127.0.0.1","localhost","192.168.10.102"};

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        NettyMessage message  = (NettyMessage) msg;

        /**
         * 由于所有人都可以是服务端，也可以是客户端
         */

        if(message.getHeader()!=null&&
        message.getHeader().getType() == MessageType.LOGIN_REQ.value()){
            String nodeIndex = ctx.channel().remoteAddress().toString();
            NettyMessage loginResp = null;
            //判断是否重复登陆
            if(nodeCheck.containsKey(nodeIndex)){
                //是重复登陆，打回
                loginResp =buildResponse((byte) -1);//-1验证错误
            }else{
                //源地址 127.0.0.1：12281
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                String ip = address.getAddress().getHostAddress();
                boolean isOK = false;
                for(String wip : whiteList){//是否在白名单中
                    if(wip.equals(ip)){
                        isOK=true;
                        break;
                    }
                }

                loginResp = isOK?buildResponse((byte) 0):buildResponse((byte) -1);

                if(isOK){
                    //表示已经登陆
                    nodeCheck.put(nodeIndex,true);
                }


            }
            System.out.println(" 这个登陆响应 是: "+loginResp+" body ["+loginResp.getBody()+"]");
            ctx.writeAndFlush(loginResp);
        }else{
            ctx.fireChannelRead(msg);
        }

    }

    public NettyMessage buildResponse(byte result){
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setType(MessageType.LOGIN_RESP.value());
        message.setHeader(header);
        message.setBody(result);
        return message;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //发生了异常，删除缓存
        nodeCheck.remove(ctx.channel().remoteAddress().toString());
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }
}
