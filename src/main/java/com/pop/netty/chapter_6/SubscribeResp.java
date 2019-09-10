package com.pop.netty.chapter_6;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Pop
 * @date 2019/9/11 0:06
 */
@Data
public class SubscribeResp implements Serializable {
    private static final long serialVersionUID = -6576643810843611725L;
    private int subReqID;
    private int respCode;
    private String desc;

    @Override
    public String toString() {
        return "SubscribeResp{" +
                "subReqID=" + subReqID +
                ", respCode=" + respCode +
                ", desc='" + desc + '\'' +
                '}';
    }
}
