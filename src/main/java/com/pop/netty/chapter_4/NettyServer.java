package com.pop.netty.chapter_4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Pop
 * @date 2019/9/9 23:06
 */
@Slf4j
public class NettyServer {

    public void bind(int port){

        //配置服务端的NIO线程组

            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChildChannelHandler());
            //绑定端口，同步等待成功，这是一个同步方法，等待绑定操作完成后返回
            ChannelFuture future = b.bind(port).sync();
            log.info("服务器正在监听："+port);
            //等待服务端监听端口关闭,服务器链路关闭之后退出main函数
            future.channel().closeFuture().sync();
            log.info("服务器已经退出");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //释放资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel>{

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            //解决粘包问题
            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
            socketChannel.pipeline().addLast(new StringDecoder());//可以直接从msg获得string对象，不需要转换
            socketChannel.pipeline().addLast(new TimeServerHandler());
        }
    }

    public static void main(String[] args) {
        new NettyServer().bind(8080);
    }

}
