package com.pop.netty.chapter_14.utils;

/**
 * @author Pop
 * @date 2019/9/17 22:15
 */
public enum MessageType {

    BUSINESS_REQ(0),
    BUSINESS_RESP(1),
    BUSINESS_ONEWAY(2),
    LOGIN_REQ(3),
    LOGIN_RESP(4),
    PING_PONG_REQ(5),
    PING_PONG_RESP(6);

    private int value;
    MessageType(int value) {
        this.value=value;
    }
    public byte value(){
        return (byte) value;
    }
}
