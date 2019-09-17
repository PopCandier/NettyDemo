package com.pop.netty.chapter_14;

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
        return 0;
    }

    @Override
    public int read(byte[] bytes, int i, int i1) throws IOException {
        return 0;
    }

    @Override
    public int available() throws IOException {
        return byteBuf.readableBytes();
    }

    @Override
    public long skip(long l) throws IOException {
        return 0;
    }

    @Override
    public void close() throws IOException {

    }
}
