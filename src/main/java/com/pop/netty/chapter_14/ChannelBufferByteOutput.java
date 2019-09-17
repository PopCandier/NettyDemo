package com.pop.netty.chapter_14;

import io.netty.buffer.ByteBuf;
import org.jboss.marshalling.ByteOutput;

import java.io.IOException;

/**
 * @program: nio-netty
 * @description:
 * @author: Pop
 * @create: 2019-09-17 16:36
 **/
public class ChannelBufferByteOutput implements ByteOutput {

    private final ByteBuf buffer;

    public ByteBuf getBuffer() {
        return buffer;
    }

    public ChannelBufferByteOutput(ByteBuf buffer) {
        this.buffer = buffer;
    }

    @Override
    public void write(int i) throws IOException {
        buffer.writeByte(i);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        buffer.writeBytes(bytes);
    }

    @Override
    public void write(byte[] bytes, int i, int i1) throws IOException {
        buffer.writeBytes(bytes,i,i1);
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void flush() throws IOException {

    }
}
