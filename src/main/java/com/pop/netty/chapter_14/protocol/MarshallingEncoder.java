package com.pop.netty.chapter_14.protocol;

import com.pop.netty.chapter_8.MarshallingCodeCFactory;
import io.netty.buffer.ByteBuf;
import org.jboss.marshalling.Marshaller;

import java.io.IOException;

/**
 * @program: nio-netty
 * @description:
 * @author: pop
 * @create: 2019-09-17 15:49
 **/
public class MarshallingEncoder {

    private static final byte[] LENGTH_PLACEHOLDER=
            new byte[4];

    Marshaller marshaller;

    public MarshallingEncoder() throws IOException {
        marshaller = MarshallingCodeCFactory.buildMarshalling();
    }

    protected void encode(Object msg, ByteBuf out) throws  Exception{

        try{
            //已经写到了哪个位置
            int lengthPos = out.writerIndex();
            //占位，用于记录Object对象编码后到底长度 ,这里是四个
            out.writeBytes(LENGTH_PLACEHOLDER);
            //使用代理对象，放置marshaller写完后关闭byte buf
            ChannelBufferByteOutput output =
                    new ChannelBufferByteOutput(out);
//        BufferByteOutput
            marshaller.start(output);
            marshaller.writeObject(msg);
            //编码结束
            marshaller.finish();
            //设置对象长度         已经写到哪里-刚开始的长度-我们设置的4个字节长度
            out.setInt(lengthPos,out.writerIndex()-lengthPos-4);
        }finally {
            marshaller.close();
        }
    }
}
