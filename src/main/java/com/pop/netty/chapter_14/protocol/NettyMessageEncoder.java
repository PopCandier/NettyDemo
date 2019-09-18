package com.pop.netty.chapter_14.protocol;


import com.pop.netty.chapter_14.entity.Header;
import com.pop.netty.chapter_14.entity.NettyMessage;
import com.pop.utils.CharsetUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
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
public class NettyMessageEncoder extends MessageToByteEncoder<NettyMessage> {

    MarshallingEncoder marshallingEncoder;

    public NettyMessageEncoder() throws IOException {
        this.marshallingEncoder = new MarshallingEncoder();
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, NettyMessage message, ByteBuf sendBuf) throws Exception {
        if(message==null||message.getHeader()==null){
            throw new Exception(" 解码后的 消息为null");
        }

        //进行编码操作
        Header header = message.getHeader();
        sendBuf.writeInt(header.getCrcCode());//4
        sendBuf.writeInt(header.getLength());//4
        sendBuf.writeLong(header.getSessionID());//8
        sendBuf.writeByte(header.getType());//1
        sendBuf.writeByte(header.getPriority());//1
        sendBuf.writeInt(header.getAttachment().size());//4
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
        }
        //相当于更新header中的length字段   readableBytes 与writerIndex是相同的
        //解释一下这里为什么要-4，如果这是一个没有包含数据，也就是body的包
        //这里一共是26位bit，然后我们会在4这个位置重新将数据长度设置进去，但是由于前面有四个没有意义的占位codec和字符的长度，所以我们要剔除
        //前面的占位符，后面才是真正的数据体
        sendBuf.setInt(4,sendBuf.readableBytes()-8);
    }

}
