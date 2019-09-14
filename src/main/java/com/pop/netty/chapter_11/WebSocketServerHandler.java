package com.pop.netty.chapter_11;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;

import javax.xml.soap.Text;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Pop
 * @date 2019/9/14 19:33
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = Logger.
            getLogger(WebSocketServerHandler.class.getName());

    WebSocketServerHandshaker handshaker = null;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        //传统的Http接入、websocket发起的第一个握手请求为http请求
        if(msg instanceof FullHttpRequest){
            //处理http请求
            handlerHttpRequest(ctx,(FullHttpRequest)msg);
        }
        //websocket 接入
        else if(msg instanceof WebSocketFrame){
            handleWebSocketFrame(ctx,(WebSocketFrame)msg);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        //是否是关闭链路的指令
        if(frame instanceof CloseWebSocketFrame){
            handshaker.close(ctx.channel(),
                    ((CloseWebSocketFrame) frame).retain());// retain 保持
            return;
        }
        //判断是否是ping消息
        if(frame instanceof PingWebSocketFrame){//ping -pong
            ctx.channel().write(
                    new PongWebSocketFrame(frame.content().retain())
            );
            return ;
        }

        //只支持文本消息，不支持二进制消息
        if(!(frame instanceof TextWebSocketFrame)){
            throw  new UnsupportedOperationException(
                    String.format("%s frame types not supported",
                            frame.getClass().getName())
            );
        }

        //返回应答消息
        String request = ((TextWebSocketFrame) frame).text();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("%s 接受到 %s",ctx.channel(),request));
        }

        ctx.channel().write(
                new TextWebSocketFrame(request + ", 欢迎使用 Netty WebSocket 服务")
        );

        ctx.flush();

    }

    private void handlerHttpRequest(ChannelHandlerContext ctx, FullHttpRequest msg) {
        //如果http解码失败，返回http异常
        if(!msg.decoderResult().isSuccess()||
            "webscoket".equals(msg.headers().get("Upgrade"))){
            sendHttpReponse(ctx,msg,new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST
            ));
            return ;
        }

        //构造握手响应返回，本机测试
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory("ws://localhost:8080/websocket",null,false);

         handshaker=wsFactory.newHandshaker(msg);

        if(handshaker == null){
            //返回不支持的请求
            WebSocketServerHandshakerFactory.
                    sendUnsupportedVersionResponse(ctx.channel());
        }else{
            handshaker.handshake(ctx.channel(),msg);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void sendHttpReponse(ChannelHandlerContext ctx, FullHttpRequest request, DefaultFullHttpResponse response) {
        //返回应答给客户端
        if(response.status().code()!=200){
            ByteBuf buf = Unpooled.copiedBuffer(response.status().toString(),
                    Charset.forName("utf-8"));

            response.content().writeBytes(buf);

            buf.release();
            response.headers().set("Content-Length",response.content().readableBytes());
        }

        ChannelFuture future = ctx.channel().writeAndFlush(response);

        //如果是非 Keep-alive 关闭连接
        if(!"keep-alive".equalsIgnoreCase(request.headers().get("Connection"))||
            response.status().code()!=200){
            future.addListener(ChannelFutureListener.CLOSE);
        }

    }
}
