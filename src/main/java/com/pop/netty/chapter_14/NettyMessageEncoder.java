package com.pop.netty.chapter_14;


import com.pop.utils.CharsetUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @program: nio-netty
 * @description: 用于NettyMessage的编码
 * @author: pop
 * @create: 2019-09-17 15:06
 **/
public class NettyMessageEncoder extends MessageToMessageEncoder<NettyMessage> {

    MarshallingEncoder marshallingEncoder;

    public NettyMessageEncoder() throws IOException {
        this.marshallingEncoder = new MarshallingEncoder();
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, NettyMessage message, List<Object> list) throws Exception {

        if(message!=null||message.getHeader()==null){
            throw new Exception(" 解码后的 消息为null");
        }

        ByteBuf sendBuf = Unpooled.buffer();
        //进行编码操作
        Header header = message.getHeader();
        sendBuf.writeInt(header.getCrcCode());
        sendBuf.writeInt(header.getLength());
        sendBuf.writeLong(header.getSessionID());
        sendBuf.writeByte(header.getType());
        sendBuf.writeByte(header.getPriority());
        sendBuf.writeInt(header.getAttachment().size());
        //以上为header的编码
        String key = null;
        byte[] keyArray = null;
        Object value = null;
        for(Map.Entry<String,Object> param:message.getHeader().getAttachment().entrySet()){
            key = param.getKey();
            keyArray = key.getBytes(CharsetUtils.UTF_8);
            sendBuf.writeInt(keyArray.length);//记录这个键的长度，方便读取
            sendBuf.writeBytes(keyArray);//记录内容
            value = param.getValue();
            marshallingEncoder.encode(value,sendBuf);
        }
        key = null;
        keyArray = null;
        value = null;
        if(message.getBody()!=null){
            marshallingEncoder.encode(message.getBody(),sendBuf);
        }else{
            sendBuf.writeInt(0);
            sendBuf.setInt(4,sendBuf.readableBytes());
        }

    }
}