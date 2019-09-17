package com.pop.netty.chapter_14;

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

    protected Object decode(ByteBuf in){

        int objectSize = in.readInt();
        ByteBuf buf = in.slice(in.readerIndex(),objectSize);
//        ByteInput input = new Chann
        return null;
    }

}
