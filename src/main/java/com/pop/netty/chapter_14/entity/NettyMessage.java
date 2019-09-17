package com.pop.netty.chapter_14.entity;

import lombok.Data;

/**
 * @author Pop
 * @date 2019/9/17 0:00
 */
@Data
public final class NettyMessage {

    private Header header;//消息头
    private Object body;//消息体

    @Override
    public String toString() {
        return "NettyMessage{" +
                "header=" + header +
                ", body=" + body +
                '}';
    }
}
