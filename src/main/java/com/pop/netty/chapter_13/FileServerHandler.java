package com.pop.netty.chapter_13;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * @author Pop
 * @date 2019/9/16 22:29
 */
public class FileServerHandler extends SimpleChannelInboundHandler<String> {

    private static final String CR=System.getProperty("line.separator");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

        File file = new File(msg);

        if(file.exists()){

            if(!file.isFile()){
                ctx.writeAndFlush(" 你不是一个文件 : "+file+CR);
                return;
            }

            ctx.write(file+ " "+ file.length()+ CR);
            //打开这个文件
            RandomAccessFile randomAccessFile = new RandomAccessFile(msg,"r");
            FileRegion region = new DefaultFileRegion(
                    randomAccessFile.getChannel(),0,randomAccessFile.length()
            );
            ctx.write(region);
            ctx.writeAndFlush(CR);
            randomAccessFile.close();
        }else{
            ctx.writeAndFlush(" 文件没找到 "+file+CR);
        }


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
