package com.pop.netty.chapter_14.protocol;

import io.netty.buffer.ByteBuf;
import org.jboss.marshalling.ByteInput;

import java.io.IOException;

/**
 * @program: nio-netty
 * @description:
 * @author: Pop
 * @create: 2019-09-17 19:56
 **/
public class ChannelBufferByteInput implements ByteInput {

    private final ByteBuf byteBuf;

    public ChannelBufferByteInput(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    @Override
    public int read() throws IOException {
        /**
         * Returns true if and only if (
         * 即是写到哪  - 读到哪
         * this.writerIndex - this.readerIndex)
         * is greater than 0.
         */
        if(byteBuf.isReadable()){
            return byteBuf.readByte()&0xff;
        }
        return -1;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        return read(bytes,0,bytes.length);
    }

    @Override
    public int read(byte[] bytes, int i, int i1) throws IOException {
        int available = available();
        if(available==0){return -1;}
        /**
         * 可读长度和读到哪里取最小值，放置不越界
         */
        i1 = Math.min(available,i1);
        byteBuf.readBytes(bytes,i,i1);
        return i1;
    }

    @Override
    public int available() throws IOException {
        return byteBuf.readableBytes();
    }

    @Override
    public long skip(long l) throws IOException {
        int readable = byteBuf.readableBytes();
        if(readable<l){
            //可能越界，因为只读的数目是小于传入的数目的
            l = readable;
        }
        byteBuf.readerIndex((int) (byteBuf.readerIndex()+l));
        return l;
    }

    @Override
    public void close() throws IOException {

    }
}
