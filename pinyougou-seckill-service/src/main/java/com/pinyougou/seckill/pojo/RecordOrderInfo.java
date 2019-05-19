package com.pinyougou.seckill.pojo;

import java.io.Serializable;

public class RecordOrderInfo implements Serializable {
    private String userId;//用户ID 也就是登录的用户名
    private Long id; //用户抢购的商品的ID
    public RecordOrderInfo() {

    }

    public RecordOrderInfo(String userId, Long id) {
        this.userId = userId;
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}