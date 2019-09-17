package com.pop.netty.chapter_14.client;


import com.pop.netty.chapter_14.processor.HeartBeatReqHandler;
import com.pop.netty.chapter_14.processor.LoginAuthReqHandler;
import com.pop.netty.chapter_14.protocol.NettyMessageDecoder;
import com.pop.netty.chapter_14.protocol.NettyMessageEncoder;
import com.pop.netty.chapter_14.utils.NettyConstant;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Pop
 * @date 2019/9/17 23:27
 */
public class NettyClient {

    private ScheduledExecutorService executor
            = Executors.newScheduledThreadPool(1);
    EventLoopGroup group = new NioEventLoopGroup();
    public void connect(int port,String host){

        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            ChannelPipeline pipeline = ch.pipeline();
                            //为什么要移动四个字节呢，因为crcCode占有4个，但是这个是没有意义的
                            pipeline.addLast(
                                    new NettyMessageDecoder(1024*1024,
                                            4,4)
                            );

                            pipeline.addLast("MessageEncoder",
                                    new NettyMessageEncoder());

                            /**
                             * 这里用于心跳超时，
                             * 利用ReadTimeoutHandler机制
                             * 当一定周期 默认值 50s，没有读取到任何消息
                             * 就关闭链路
                             *
                             * 如果是客户端，重新发起连接，如果是服务端
                             * 释放资源，清除客户端登陆缓存消息，等待服务器重连
                             *
                             */
                            pipeline.addLast("readTimeHandler",
                                    new ReadTimeoutHandler(50));

                            //权限认证 处理由服务器返回的心跳
                            pipeline.addLast("LoginAuthHandler",
                                    new LoginAuthReqHandler());

                            pipeline.addLast("HeartBeatHandler",
                                    new HeartBeatReqHandler());

                        }
                    });
            //发起异步连接
            //前者是要发起的ip地址，后者是自己的
            ChannelFuture future = b.connect(
                    new InetSocketAddress(host,port),
                    new InetSocketAddress(NettyConstant.HOST,NettyConstant.PORT)
            ).sync();

            future.channel().closeFuture().sync();//如果客户端监听到了断线异常，会断开
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //所有资源释放完成之后，清空资源，再次发起重连操作
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(5);

                        //休息五秒后，不停的连接
                        connect(NettyConstant.REMOTEIP,NettyConstant.REMOTEHOST);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    public static void main(String[] args) {
        new NettyClient().connect(NettyConstant.REMOTEIP,NettyConstant.REMOTEHOST);
    }


}
