package com.pop.netty.chapter_6;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Pop
 * @date 2019/9/10 23:56
 */
@Data
public class SubscribeReq implements Serializable {


    private static final long serialVersionUID = 5138691000430567579L;

    private int subReqID;
    private String userName;
    private String productName;
    private String phoneNumber;
    private String address;

    @Override
    public String toString() {
        return "SubscribeReq{" +
                "subReqID=" + subReqID +
                ", userName='" + userName + '\'' +
                ", productName='" + productName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
