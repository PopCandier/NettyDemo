package com.pop.netty.chapter_14.protocol;

import com.pop.netty.chapter_14.entity.Header;
import com.pop.netty.chapter_14.entity.NettyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: nio-netty
 * @description: NettyMessageDecoder
 * @author: Pop
 * @create: 2019-09-17 17:35
 **/
public class NettyMessageDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * LengthFieldBasedFrameDecoder
     * 它支持自动的TCP粘包和半包处理，只需要
     * 要给出表示消息长度的字偏移量和消息长度
     * 所占的字节数，Netty就能自动实现对半包的处理
     * 对用父类的解码方法后，返回的就是整包消息
     * 或者为空
     */

    MarshallingDecoder marshallingDecoder;

    public NettyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) throws IOException {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
        marshallingDecoder = new MarshallingDecoder();
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx,in);
        if(frame==null){//如果为空说明是半包消息
            //直接返回继续由IO线程读取后续的流
            return null;
        }
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        header.setCrcCode(in.readInt());
        header.setLength(in.readInt());
        header.setSessionID(in.readLong());
        header.setType(in.readByte());
        header.setPriority(in.readByte());

        //从这里获得后面对象body的长度和内容
        int size = in.readInt();
        if(size>0){
            Map<String,Object> attch = new HashMap<String,Object>(size);
            int keySize=0;
            byte[] keyArray = null;
            String key = null;
            for(int i =0;i<size;i++){
                //这个size是这个对象的长度
                //第一个值是获取这个key有多长
                keySize = in.readInt();
                keyArray = new byte[keySize];
                in.readBytes(keyArray);//写入这么长的key1的内容
                //获得key
                key = new String(keyArray,"utf-8");
                attch.put(key,marshallingDecoder.decode(in));
            }
            keyArray = null;
            key = null;
            header.setAttachment(attch);

        }
        if(in.readableBytes()>4){
            message.setBody(marshallingDecoder.decode(in));
        }

        message.setHeader(header);
        return message;
    }
}
