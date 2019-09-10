package com.pop.netty.chapter_4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Pop
 * @date 2019/9/9 23:21
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();//将缓冲区的数据写到SocketChannel中
    }
    private int counter;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

//        ByteBuf buf = (ByteBuf) msg;
//        /*
//        * 这里将msg强制转化为ByteBuf对象，这是Netty中的对象，通过buff中的获得缓冲区可以
//        * 阅读的字节数创建字节数组，然后将内容复制到新建的byte数组中
//        * */
//        byte[] req = new byte[buf.readableBytes()];
//        buf.readBytes(req);//写到字节数组中
//        String body = new String(req,"utf-8").
//                substring(0,req.length-System.getProperty("line.separator").length());
        //使用了StringDecoder
        String body = (String) msg;

        System.out.println("The time server receive order : "+body+" ; the counter: "+counter++);

        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)?
                new java.util.Date(System.currentTimeMillis()).toString():"Bad Order";
        currentTime+=System.getProperty("line.separator");




//        System.out.println("服务器收到信息: "+body);



        //返回 这边是简单的回答
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.writeAndFlush(resp);//放入缓冲区
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();//发生异常的时候，释放资源

    }
}
