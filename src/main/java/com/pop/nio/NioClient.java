package com.pop.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Pop
 * @date 2019/9/8 21:57
 */
public class NioClient {

    /**
     * 客户端和服务端没什么区别，最主要的区别在于channel改为了SocketChannel
     */
    private Selector selector = null;
    private InetSocketAddress inetSocketAddress = null;
    private SocketChannel client =null;
    private Charset charset = Charset.forName("utf-8");

    public NioClient(String host,int port) throws IOException {
        selector = Selector.open();
        inetSocketAddress = new InetSocketAddress(host,port);
        client = SocketChannel.open();//默认会提供一个

        //设置一些参数
        boolean connected = client.connect(inetSocketAddress);
        client.configureBlocking(false);
        //为是否连接成功选择注册一些什么参数
        if(connected){
            //连接成功，那么表示，我可以接受从服务端来的读写的数据了
            client.register(selector, SelectionKey.OP_READ);
        }else{
            /**
             * 连接不成功可能会有两种情况，第一种是请求发送成功了，但是还没有得
             * 到回执
             * 也就是物理链路没有建立
             * 另外一种则是真的连接失败
             */
            client.register(selector,SelectionKey.OP_CONNECT);
        }
    }

    public void session(){

        new Reader().start();
        new Writer().start();

    }

    public static void main(String[] args) throws IOException {
        new NioClient("localhost",8080).session();
    }

    /**
     * 我们新建两个线程，一个是可以从控制台输入命令传达给服务器的
     * 另外一个是用来接收服务器返回给我们的请求的
     */

    private class Reader extends Thread{
        @Override
        public void run() {

            //这一步和服务器一样
            while(true){

                try {
                    int readyChannels = selector.select();
                    if(readyChannels==0)continue;
                    Set<SelectionKey> keySet = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keySet.iterator();
                    while(iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        try {
                            process(key);
                        } catch (Exception e) {
                            if(null!=key){
                                key.cancel();
                                if(key.channel()!=null){
                                    key.channel().close();
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

        }

        private void process(SelectionKey key) {

            if(key.isValid()){

                if(key.isConnectable()){
                    System.out.println("客户端已经建立连接");
                    key.interestOps(SelectionKey.OP_READ);
                }
                if(key.isReadable()){
                    SocketChannel channel= (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    StringBuilder result = new StringBuilder();
                    try {
                        while(channel.read(buffer)>0){
                            buffer.flip();
                            result.append(charset.decode(buffer));
                        }
                        System.out.println(result.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        }
    }

    /**
     * 写线程
     */
    private class Writer extends Thread{

        @Override
        public void run() {

            Scanner scanner = new Scanner(System.in);
            try {
                while(scanner.hasNextLine()){
                    String line = scanner.nextLine();
                    client.write(charset.encode(line));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                scanner.close();
            }

        }
    }

}
