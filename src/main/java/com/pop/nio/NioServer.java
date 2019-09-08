package com.pop.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Pop
 * @date 2019/9/8 21:57
 */
public class NioServer {

    /**
     *多路复用器
     */
    private Selector selector;

    /**
     * ip地址
     */
    private InetSocketAddress socketAddress = null;
    /**
     * 服务端channel
     */
    private ServerSocketChannel server = null;

    private volatile boolean stop =true;

    private Charset charset = Charset.forName("utf-8");

    public NioServer(int port)  {
        try {
            socketAddress = new InetSocketAddress(port);
            selector = Selector.open();
            server = ServerSocketChannel.open();
            server.bind(socketAddress);
            server.configureBlocking(false);//设置非阻塞
            //将服务器自己的channel注册到选择其中去
            server.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("已经在："+port+" 端口监听");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void stop(){this.stop=false;}

    public void listen() {

        while(this.stop){

            try {
                //开始监听
                int wait = selector.select();//等待监听
                if(wait==0){continue;}//如果是0，则接着监听
                Set<SelectionKey> keys = selector.selectedKeys();
                /**
                 * 获得注册到多路复用器上的key
                 */
                Iterator<SelectionKey> iterator = keys.iterator();
                while(iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    //因为已经获得，所以将这个注册的key删除
                    iterator.remove();
                    try {
                        process(key);
                    } catch (Exception e) {
                        if(null!=key){//将次channel的注册key取消掉，并关闭通道释放资源
                            key.cancel();
                            if(null!=key.channel()){
                                key.channel().close();
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {

            }
        }
        /**
         * 多路复用器关闭后，注册在上面的channel和pipe都会被关闭
         */
        if(null!=selector){
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * <p> 对这个key进行处理</p>
     * @param key
     */
    private void process(SelectionKey key) throws IOException {

        if(key.isValid()){//是否已经是被cancel的key

            if(key.isAcceptable()){//之前在构造器环节，我们已经对服务器的key注册了accept事件
                //所以，如果符合这一条件，那么应该是服务器端有了新的连接
                ServerSocketChannel serverSocketChannel= (ServerSocketChannel) key.channel();
                //接着，我们将这条由服务端的channel中取出前来连接的client连接
                SocketChannel client = serverSocketChannel.accept();
                //将这条通过设置为非阻塞，方便之后异步读写
                client.configureBlocking(false);
                //并且为这条客户端请求，注册到多路复用器上，并设置为可读
                client.register(selector,SelectionKey.OP_READ);
                //再次注册为可接受状态，表示这条以及处理完了，可以接受其他的客户端连接请求
                key.interestOps(SelectionKey.OP_ACCEPT);
                //并且返回信息，告诉这条客户端连接成功
                client.write(charset.encode("连接已经建立"));
            }

            if(key.isReadable()){//由于之前，我们可以设置为可读请求，所以现在，我们就可以处理读请求
                SocketChannel client= (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                StringBuilder result = new StringBuilder();

                    /**
                     * 返回值大于0，读到了字节，对字节进行编码
                     * 返回值等于0，没有读到字节，属于正常情况，忽略
                     * 返回值为-1，链路已经关闭，需要关闭channel释放资源
                     */
                    while(client.read(buffer)>0){//由于这个读我们之前设置为异步的，所以等待返回
                        buffer.flip();
                        result.append(charset.decode(buffer));
                    }
                    //并且将这次的key，再次设置为key的状态，表示接着接受key请求
                    //这个需要再次注册一次，因为每一次循环的时候，key都会被remove
                    key.interestOps(SelectionKey.OP_READ);
                    System.out.println(result.toString());

                client.write(charset.encode("服务端已经收到了你的消息，消息为："+result.toString()));
            }

            if(key.isWritable()){//可写状态
                // todo

            }

        }

    }

    public static void main(String[] args) {
        new NioServer(8080).listen();
    }
}
