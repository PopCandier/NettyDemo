package com.pop.netty.chapter_14.protocol;

import com.pop.netty.chapter_8.MarshallingCodeCFactory;
import io.netty.buffer.ByteBuf;
import org.jboss.marshalling.ByteInput;
import org.jboss.marshalling.Unmarshaller;

import java.io.IOException;

/**
 * @program: nio-netty
 * @description:
 * @author: Pop
 * @create: 2019-09-17 16:43
 **/
public class MarshallingDecoder {

    private final Unmarshaller unmarshaller;

    public MarshallingDecoder() throws IOException {
        unmarshaller = MarshallingCodeCFactory.buildUnMarshalling();
    }

    protected Object decode(ByteBuf in) throws IOException {

        int objectSize = in.readInt();
        //这时候的buf里，存的是body，也就是object对象的序列化信息
        ByteBuf buf = in.slice(in.readerIndex(),objectSize);
        ByteInput input = new ChannelBufferByteInput(buf);
        try {
            unmarshaller.start(input);
            Object obj = unmarshaller.readObject();
            unmarshaller.finish();
            in.readerIndex(in.readerIndex()+objectSize);//将readerIndex游标后移
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            unmarshaller.close();
        }
        return null;
    }

}
